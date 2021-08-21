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
    val cache = when {
        job.localCache -> "localcache"
        job.remoteCache -> "remotecache"
        else -> "nocache"
    }
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
    val jobsec = job.buildTool.securityContext
    val ctx = SecurityContext()

    if (jobsec != null) {
        ctx.privileged = jobsec.privileged
        ctx.runAsUser = jobsec.runAsUser
    }
    return ctx
}

class Test(job: Job) : Pod() {
    init {
        val repoName = genRepoName(job.repo)
        val jobName = genTestTypeName(job)
        val jobs = K8s.listTestJobs().filter { it.metadata?.name?.startsWith(genTestTypeName(job)) == true }
        val config = readConfig()
        val id = jobs.map { it.metadata.name }
            .filter { it.startsWith(jobName) }
            .map { it.substring(jobName.length + 1) }
            .maxOfOrNull { it.toInt() + 1 } ?: 0


        metadata {
            name = "${jobName}-$id"
            annotations = genAnnotations(job)
        }
        spec {
            initContainers = listOfNotNull(
                if (job.localCache) {
                    newContainer {
                        name = "setup"
                        image = "alpine"
                        command = listOf("chmod", "-R", "777", job.buildTool.localCacheDir)
                        volumeMounts = listOf(
                            newVolumeMount {
                                mountPath = job.buildTool.localCacheDir
                                name = job.buildTool.name
                                subPath = job.buildTool.name
                                readOnly = false
                            }
                        )
                    }
                } else null
            )
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
                    volumeMounts = listOfNotNull(
                        newVolumeMount {
                            mountPath = config.workdir
                            name = "cbtt-repo-${job.repo.name}"
                            subPath = "repo"
                            readOnly = true
                        }, if (job.localCache) {
                            newVolumeMount {
                                mountPath = job.buildTool.localCacheDir
                                name = job.buildTool.name
                                subPath = job.buildTool.name
                                readOnly = false

                            }
                        } else null
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
            volumes = listOfNotNull(
                newVolume {
                    name = repoName
                    persistentVolumeClaim {
                        claimName = repoName
                    }
                },
                if (job.localCache) {
                    newVolume {
                        name = job.buildTool.name
                        persistentVolumeClaim {
                            claimName = job.buildTool.name
                        }
                    }
                } else null
            )
            restartPolicy = "Never"
        }
    }

    private fun genCommands(job: Job, config: Config): List<String> {
        val base = listOf("sh", "-c")
        val commands = when {
            job.remoteCache && job.push -> job.buildTool.command.remoteCache!!.push!!
            job.remoteCache && !job.push -> job.buildTool.command.remoteCache!!.noPush!!
            job.localCache && job.push -> job.buildTool.command.localCache!!.push!!
            job.localCache && !job.push -> job.buildTool.command.localCache!!.noPush!!
            job.push -> job.buildTool.command.noCache!!.push!!
            else -> job.buildTool.command.noCache!!.noPush!!
        }
        println(job)
        val command = listOf(
//            "sleep 1000",
//            "mkdir -p /tmp",
//            "cp -rf ${config.workdir}/repo /tmp${config.workdir}",
//            "cd /tmp${config.workdir}",
            job.buildTool.command.setup,
            commands
        ).joinToString(" && ")

        return base + command
            .replace("\${name}", job.buildTool.name)
            .replace("\${cache_dir}", "${job.buildTool.localCacheDir}")
            .replace("\${workdir}", "${config.workdir}/repo")
            .replace("\${image}", "${registryPrefix}/${job.buildTool.name}")
            .replace("\${registry_url}", registryUrl)
            .replace("\${registry_username}", registryUsername)
            .replace("\${registry_password}", registryPassword)
    }
}

@Serializable
data class Job(
    val repo: Repository,
    val buildTool: BuildTool,
    val localCache: Boolean,
    val remoteCache: Boolean,
    val push: Boolean
)

