package com.jonassjodin.cbtt.k8s.repos

import com.charleskorn.kaml.Yaml
import com.jonassjodin.cbtt.config.Config
import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.lib.hash
import io.kubernetes.client.common.KubernetesObject
import io.kubernetes.client.custom.Quantity
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*

fun nameAndChecksum(repo: Repository) = Pair(genRepoName(repo), checksum(repo))
fun nameAndChecksum(o: KubernetesObject) = Pair(o.metadata!!.name!!, o.metadata!!.annotations!!["checksum"]!!)

fun genRepoName(repo: Repository) = "cbtt-repo-${repo.name}"

fun checksum(repo: Repository) = hash(Yaml.default.encodeToString(Repository.serializer(), repo))

private fun createMetadata(repo: Repository): V1ObjectMeta {
    val metadata = V1ObjectMeta()
    metadata.name = genRepoName(repo)
    metadata.annotations = mapOf(Pair("checksum", checksum(repo)))
    return metadata
}

object RepoPVC {
    fun apply(repo: Repository, namespace: String) {
        val pvc = V1PersistentVolumeClaim()
        pvc.metadata = createMetadata(repo)
        pvc.spec = createSpec()

        val api = CoreV1Api()
        api.createNamespacedPersistentVolumeClaim(namespace, pvc, null, null, null)
    }

    private fun createSpec(): V1PersistentVolumeClaimSpec {
        val spec = V1PersistentVolumeClaimSpec()
        spec.accessModes = listOf("ReadWriteOnce")
        spec.resources = V1ResourceRequirements()
        spec.resources!!.requests = mapOf(Pair("storage", Quantity("3Gi")))
        return spec
    }
}

object RepoPod {
    fun apply(config: Config, repo: Repository, namespace: String) {
        val pod = V1Pod()
        pod.metadata = createMetadata(repo)
        pod.spec = createSpec(repo, config)
        val api = CoreV1Api()
        api.createNamespacedPod(namespace, pod, null, null, null)
    }

    private fun createSpec(repo: Repository, config: Config): V1PodSpec {
        val vol = V1Volume()
        vol.name = genRepoName(repo)
        vol.persistentVolumeClaim = V1PersistentVolumeClaimVolumeSource()
        vol.persistentVolumeClaim!!.claimName = genRepoName(repo)

        val spec = V1PodSpec()
        spec.containers = createSpecContainers(repo, config)
        spec.volumes = listOf(vol)
        spec.restartPolicy = "Never"
        return spec
    }

    private fun createSpecContainers(repo: Repository, config: Config): List<V1Container> {
        val volMount = V1VolumeMount()
        volMount.mountPath = config.workdir
        volMount.name = genRepoName(repo)
        volMount.readOnly = false

        val containers = listOf(V1Container())
        containers[0].name = genRepoName(repo)
        containers[0].image = "alpine:3.13.2"
        containers[0].command = listOf("sh", "-c", createCommand(repo, config))
        containers[0].volumeMounts = listOf(volMount)
        return containers
    }

    private fun createCommand(repo: Repository, config: Config): String =
        listOf(
            "apk add git rsync",
            "git clone ${repo.url} repo",
            "mkdir ${config.workdir}/repo",
            "cd repo",
            "rsync -a ./${repo.dir}/ ${config.workdir}/repo/",
        ).joinToString(" && ")

}

