package com.jonassjodin.cbtt.k8s.repos

import com.charleskorn.kaml.Yaml
import com.jonassjodin.cbtt.config.Config
import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.lib.hash
import io.kubernetes.client.common.KubernetesObject
import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*

fun nameAndChecksum(o: KubernetesObject) = Pair(o.metadata!!.name!!, o.metadata!!.annotations!!["checksum"]!!)

private fun createMetadata(repo: Repository): V1ObjectMeta {
    val hashValue = hash(Yaml.default.encodeToString(Repository.serializer(), repo))
    val metadata = V1ObjectMeta()
    metadata.name = "cbtt-repo-${repo.name}"
    metadata.annotations = mapOf(Pair("checksum", hashValue))
    return metadata
}

class RepoPVC(repo: Repository, private val namespace: String) {
    val k8sPvc = V1PersistentVolumeClaim()

    init {
        k8sPvc.metadata = createMetadata(repo)
        k8sPvc.spec = createSpec()
    }

    fun apply() {
        val api = CoreV1Api()
        api.createNamespacedPersistentVolumeClaim(namespace, k8sPvc, null, null, null)
    }

    private fun createSpec(): V1PersistentVolumeClaimSpec {
        val spec = V1PersistentVolumeClaimSpec()
        spec.accessModes = listOf("ReadWriteOnce")
        spec.resources = V1ResourceRequirements()
        spec.resources!!.requests = mapOf(Pair("storage", Quantity("3Gi")))
        return spec
    }
}

class RepoJob(private val config: Config, private val repo: Repository, private val namespace: String) {
    val k8sJob = V1Job()

    init {
        k8sJob.metadata = createMetadata(repo)
        k8sJob.spec = createSpec()
    }

    fun apply() {
        val api = BatchV1Api()
        api.createNamespacedJob(namespace, k8sJob, null, null, null)
    }

    private fun createSpec(): V1JobSpec {
        val vol = V1Volume()
        vol.name = "cbtt-repo-${repo.name}"
        vol.persistentVolumeClaim = V1PersistentVolumeClaimVolumeSource()
        vol.persistentVolumeClaim!!.claimName = "cbtt-repo-${repo.name}"

        val spec = V1JobSpec()
        spec.backoffLimit = 1
        spec.template = V1PodTemplateSpec()
        spec.template!!.spec = V1PodSpec()
        spec.template!!.spec!!.containers = createSpecContainers()
        spec.template!!.spec!!.volumes = listOf(vol)
        spec.template!!.spec!!.restartPolicy = "Never"
        return spec
    }

    private fun createSpecContainers(): List<V1Container> {
        val volMount = V1VolumeMount()
        volMount.mountPath = config.workdir
        volMount.name = "cbtt-repo-${repo.name}"
        volMount.readOnly = false //TODO: CHANGE DIS

        val containers = listOf(V1Container())
        containers[0].name = "cbtt-repo-${repo.name}"
        containers[0].image = "alpine:3.13.2"
        containers[0].command = listOf("sh", "-c", createCommand())
        containers[0].volumeMounts = listOf(volMount)
        return containers
    }

    private fun createCommand() = listOf(
        "apk add git rsync",
        "git clone ${repo.url} repo",
        "echo ${config.workdir}",
        "ls -la ${config.workdir}",
        "mv repo${repo.dir} ${config.workdir}",
//        "cd repo && rsync -a ${repo.dir}/ ${config.workdir}/repo/",
    ).joinToString(" && ")
}

