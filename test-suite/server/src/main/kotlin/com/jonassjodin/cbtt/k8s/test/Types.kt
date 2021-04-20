package com.jonassjodin.cbtt.k8s.test

import com.jonassjodin.cbtt.config.BuildTool
import com.jonassjodin.cbtt.config.Config
import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.config.readConfig
import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.k8s.repos.genRepoName
import com.jonassjodin.cbtt.lib.getEnv
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*
import kotlinx.serialization.Serializable

val registryUrl = getEnv("REGISTRY_URL")
val registryPrefix = getEnv("REGISTRY_PREFIX")
val registryUsername = getEnv("REGISTRY_USERNAME")
val registryPassword = getEnv("REGISTRY_PASSWORD")

object Test {
    private fun genSuffix(job: Job): String {
        val cache = if (job.cache) "cache" else "nocache"
        val push = if (job.push) "push" else "nopush"
        return "$cache-$push"
    }

    private fun genName(job: Job) = "cbtt-test-${job.buildTool.name}-${job.repo.name}"
    private fun genIDName(job: Job) = "${genName(job)}-${genSuffix(job)}"

    fun apply(job: Job, namespace: String) {
        val jobs = K8s.listTestJobs().filter { it.metadata?.name?.startsWith(genIDName(job)) == true }
        val id = jobs.size

        val config = readConfig()
        val k8sPod = V1Pod()
        k8sPod.metadata = createMetadata(job)
        k8sPod.spec = createSpec(job, config)
        k8sPod.metadata!!.name = "${k8sPod.metadata?.name}-$id"

        val api = CoreV1Api()
        api.createNamespacedPod(namespace, k8sPod, null, null, null)
    }

    private fun createMetadata(job: Job): V1ObjectMeta {
        val metadata = V1ObjectMeta()
        metadata.name = genIDName(job)

        val annotations = mutableMapOf<String, String>()
        if (job.buildTool.securityContext?.apparmor != null) {
            val key = "container.apparmor.security.beta.kubernetes.io/cbtt-test-${job.buildTool.name}-${job.repo.name}"
            annotations[key] = job.buildTool.securityContext.apparmor
        }
        if (job.buildTool.securityContext?.seccomp != null) {
            val key = "container.seccomp.security.alpha.kubernetes.io/cbtt-test-${job.buildTool.name}-${job.repo.name}"
            annotations[key] = job.buildTool.securityContext.seccomp
        }
        metadata.annotations = annotations

        return metadata
    }

    private fun createSpec(job: Job, config: Config): V1PodSpec {
        val vol = V1Volume()
        vol.name = genRepoName(job.repo)
        vol.persistentVolumeClaim = V1PersistentVolumeClaimVolumeSource()
        vol.persistentVolumeClaim!!.claimName = genRepoName(job.repo)

        val spec = V1PodSpec()
        spec.containers = createSpecContainers(job, config)
        spec.volumes = listOf(vol)
        spec.restartPolicy = "Never"

        return spec
    }

    private fun createSpecContainers(job: Job, config: Config): List<V1Container> {
        val volMount = V1VolumeMount()
        volMount.mountPath = config.workdir
        volMount.name = "cbtt-repo-${job.repo.name}"
        volMount.readOnly = true

        val containers = listOf(V1Container())
        containers[0].name = genName(job)
        containers[0].image = job.buildTool.image
        containers[0].env = job.buildTool.env?.map { (k, v) ->
            val envVar = V1EnvVar()
            envVar.name = k
            envVar.value = v
            envVar
        }
        containers[0].command = genCommands(job, config)
        containers[0].volumeMounts = listOf(volMount)
        containers[0].securityContext = V1SecurityContext()
        if (job.buildTool.securityContext?.privileged == true) {
            containers[0].securityContext!!.privileged = true
        }
        if (job.buildTool.securityContext?.userID != null) {
            containers[0].securityContext!!.runAsUser = job.buildTool.securityContext.userID.toLong()
        }
        return containers
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

