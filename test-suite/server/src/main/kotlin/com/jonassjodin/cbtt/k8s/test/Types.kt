package com.jonassjodin.cbtt.k8s.test

import com.jonassjodin.cbtt.config.BuildTool
import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.config.readConfig
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.models.*
import io.kubernetes.client.util.Yaml

private fun createMetadata(job: Job): V1ObjectMeta {
    val metadata = V1ObjectMeta()
    metadata.name = "cbtt-test-${job.buildTool.name}-${job.repo.name}"
    return metadata
}

class Test(job: Job, private val namespace: String) {
    val config = readConfig()
    val k8sJob = V1Job()

    init {
        k8sJob.metadata = createMetadata(job)
        k8sJob.spec = createSpec(job)
    }

    fun apply() {
        val api = BatchV1Api()
        println(Yaml.dump(k8sJob))
        api.createNamespacedJob(namespace, k8sJob, null, null, null)
    }

    private fun createSpec(job: Job): V1JobSpec {
        val vol = V1Volume()
        vol.name = "cbtt-repo-${job.repo.name}"
        vol.persistentVolumeClaim = V1PersistentVolumeClaimVolumeSource()
        vol.persistentVolumeClaim!!.claimName = "cbtt-repo-${job.repo.name}"

        val spec = V1JobSpec()
        spec.backoffLimit = 1
        spec.template = V1PodTemplateSpec()
        spec.template.metadata = V1ObjectMeta()
        val m = mutableMapOf<String, String>()
        if (job.buildTool.securityContext?.apparmor != null) {
            m["container.apparmor.security.beta.kubernetes.io/cbtt-test-${job.buildTool.name}-${job.repo.name}"] =
                job.buildTool.securityContext.apparmor
        }
        if (job.buildTool.securityContext?.seccomp != null) {
            m["container.seccomp.security.alpha.kubernetes.io/cbtt-test-${job.buildTool.name}-${job.repo.name}"] =
                job.buildTool.securityContext.seccomp
        }
        spec.template.metadata!!.annotations = m
//        spec.template.metadata!!.annotations = job.buildTool.securityContext!!.securityAnnotations
        spec.template.spec = V1PodSpec()
        spec.template.spec!!.containers = createSpecContainers(job)
        spec.template.spec!!.volumes = listOf(vol)
        spec.template.spec!!.restartPolicy = "Never"

        return spec
    }

    private fun createSpecContainers(job: Job): List<V1Container> {
        val volMount = V1VolumeMount()
        volMount.mountPath = config.workdir
        volMount.name = "cbtt-repo-${job.repo.name}"
        volMount.readOnly = true

        val containers = listOf(V1Container())
        containers[0].name = "cbtt-test-${job.buildTool.name}-${job.repo.name}"
        containers[0].image = job.buildTool.image
        containers[0].env = job.buildTool.env?.map { (k, v) ->
            val envVar = V1EnvVar()
            envVar.name = k
            envVar.value = v
            envVar
        }
        containers[0].command = genCommands(job)
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

    private fun genCommands(job: Job) = job.buildTool.command.map {
        it.replace("\${name}", job.buildTool.name).replace("\${workdir}", config.workdir + "/repo")
    }

}

data class Job(val repo: Repository, val buildTool: BuildTool)
