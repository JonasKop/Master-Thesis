package com.jonassjodin.cbtt.k8s.test

import com.fkorotkov.kubernetes.*
import com.jonassjodin.cbtt.config.*
import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.k8s.repos.genRepoName
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.api.model.Quantity
import io.fabric8.kubernetes.api.model.SecurityContext
import kotlinx.serialization.Serializable

private fun genName(job: Job) = "cbtt-test-${job.buildTool.name}-${job.repo.name}"
private fun genTestTypeName(job: Job) = "${genName(job)}-${genSuffix(job)}"

private fun genSuffix(job: Job): String {
    val cache = if (job.cache) "cache" else "nocache"
    val push = if (job.push) "push" else "nopush"
    return "$cache-$push"
}

private fun genAnnotations(job: Job): Map<String, String> {
    val ann = mutableMapOf<String, String>()
    if (job.buildTool.securityContext?.apparmor != null) {
        val key = "container.apparmor.security.beta.kubernetes.io/cbtt-test-${job.buildTool.name}-${job.repo.name}"
        ann[key] = job.buildTool.securityContext.apparmor
    }
    if (job.buildTool.securityContext?.seccomp != null) {
        val key = "container.seccomp.security.alpha.kubernetes.io/cbtt-test-${job.buildTool.name}-${job.repo.name}"
        ann[key] = job.buildTool.securityContext.seccomp
    }
    return ann
}

private fun genSecurityContext(job: Job): SecurityContext {
    val ctx = SecurityContext()
    if (job.buildTool.securityContext?.privileged == true) {
        ctx.privileged = true
    }
    if (job.buildTool.securityContext?.userID != null) {
        ctx.runAsUser = job.buildTool.securityContext.userID.toLong()
    }
    return ctx
}

class Test(job: Job) : Pod() {
    init {
        val repoName = genRepoName(job.repo)
        val jobs = K8s.listTestJobs().filter { it.metadata?.name?.startsWith(genTestTypeName(job)) == true }
        val id = jobs.size
        val config = readConfig()

        metadata {
            name = "${genTestTypeName(job)}-$id"
            annotations = genAnnotations(job)
        }
        spec {
            containers = listOf(
                newContainer {
                    name = genName(job)
                    image = job.buildTool.image
                    env = job.buildTool.env?.map { (k, v) ->
                        newEnvVar {
                            name = k
                            value = v
                        }
                    }
                    command = genCommands(job, config)
                    volumeMounts = listOf(
                        newVolumeMount {
                            mountPath = config.workdir
                            name = "cbtt-repo-${job.repo.name}"
                            readOnly = true
                        }
                    )
                    newResourceRequirements {
                        limits = mapOf(
                            Pair("memory", Quantity(config.resources.limits.memory)),
                            Pair("cpu", Quantity(config.resources.limits.cpu))
                        )
                        requests = mapOf(
                            Pair("memory", Quantity(config.resources.requests.memory)),
                            Pair("cpu", Quantity(config.resources.requests.cpu))
                        )
                    }
                    securityContext = genSecurityContext(job)
                }
            )
            volumes = listOf(
                newVolume {
                    name = repoName
                    persistentVolumeClaim {
                        claimName = repoName
                    }
                }
            )
            restartPolicy = "Never"
        }
    }

    private fun genCommands(job: Job, config: Config): List<String> {
        val base = listOf("sh", "-c")
        val commands = when {
            job.cache && job.push -> job.buildTool.command.cache!!.push!!
            job.cache && !job.push -> job.buildTool.command.cache!!.noPush!!
            !job.cache && job.push -> job.buildTool.command.noCache!!.push!!
            else -> job.buildTool.command.noCache!!.noPush!!
        }
        val command = "${job.buildTool.command.setup} && $commands"

        return base + command
            .replace("\${name}", job.buildTool.name)
            .replace("\${workdir}", "${config.workdir}/repo")
            .replace("\${image}", "${registryPrefix}/${job.buildTool.name}")
            .replace("\${registry_url}", registryUrl)
            .replace("\${registry_username}", registryUsername)
            .replace("\${registry_password}", registryPassword)
    }
}

@Serializable
data class Job(val repo: Repository, val buildTool: BuildTool, val cache: Boolean, val push: Boolean)

