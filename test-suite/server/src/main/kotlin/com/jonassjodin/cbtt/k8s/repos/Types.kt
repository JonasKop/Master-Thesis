package com.jonassjodin.cbtt.k8s.repos

import com.charleskorn.kaml.Yaml
import com.jonassjodin.cbtt.config.Config
import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.lib.hash
import com.fkorotkov.kubernetes.*
import com.jonassjodin.cbtt.config.readConfig
import io.fabric8.kubernetes.api.model.*

class RepoPVC(repo: Repository) : PersistentVolumeClaim() {
    private val config = readConfig()

    init {
        metadata {
            name = genRepoName(repo)
            annotations = mapOf(Pair("checksum", checksum(repo, config.workdir)))
        }
        spec {
            accessModes = listOf("ReadWriteOnce")
            resources {
                requests = mapOf(Pair("storage", Quantity("3Gi")))
            }
        }
    }
}

class RepoPod(repo: Repository) : Pod() {
    private val config = readConfig()

    init {
        val repoName = genRepoName(repo)
        val shellCommand = createCommand(repo, config)

        metadata {
            name = repoName
            annotations = mapOf(Pair("checksum", checksum(repo, config.workdir)))
        }
        spec {
            containers = listOf(
                newContainer {
                    name = repoName
                    image = "alpine:3.13.2"
                    command = listOf("sh", "-c", shellCommand)
                    volumeMounts = listOf(
                        newVolumeMount {
                            mountPath = config.workdir
                            name = repoName
                            readOnly = false
                        }
                    )
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

    private fun createCommand(repo: Repository, config: Config): String =
        listOf(
            "apk add git rsync",
            "git clone ${repo.url} repo",
            "mkdir ${config.workdir}/repo",
            "cd repo",
            "rsync -a ./${repo.dir}/ ${config.workdir}/repo/",
        ).joinToString(" && ")

}

fun nameAndChecksum(repo: Repository, workdir: String) = Pair(genRepoName(repo), checksum(repo, workdir))
fun nameAndChecksum(o: HasMetadata) = Pair(o.metadata.name, o.metadata.annotations["checksum"])

fun genRepoName(repo: Repository) = "cbtt-repo-${repo.name}"

fun checksum(repo: Repository, workdir: String) =
    hash(Yaml.default.encodeToString(Repository.serializer(), repo) + workdir)

//private fun createMetadata(repo: Repository): V1ObjectMeta {
//    val metadata = V1ObjectMeta()
//    metadata.name = genRepoName(repo)
//    metadata.annotations = mapOf(Pair("checksum", checksum(repo)))
//    return metadata
//}

//object RepoPod {
//    fun apply(config: Config, repo: Repository, namespace: String) {
//        val pod = V1Pod()
//        pod.metadata = createMetadata(repo)
//        pod.spec = createSpec(repo, config)
//        val api = CoreV1Api()
//        api.createNamespacedPod(namespace, pod, null, null, null)
//    }
//
//    private fun createSpec(repo: Repository, config: Config): V1PodSpec {
//        val vol = V1Volume()
//        vol.name = genRepoName(repo)
//        vol.persistentVolumeClaim = V1PersistentVolumeClaimVolumeSource()
//        vol.persistentVolumeClaim!!.claimName = genRepoName(repo)
//
//        val spec = V1PodSpec()
//        spec.containers = createSpecContainers(repo, config)
//        spec.volumes = listOf(vol)
//        spec.restartPolicy = "Never"
//        return spec
//    }
//
//    private fun createSpecContainers(repo: Repository, config: Config): List<V1Container> {
//        val volMount = V1VolumeMount()
//        volMount.mountPath = config.workdir
//        volMount.name = genRepoName(repo)
//        volMount.readOnly = false
//
//        val containers = listOf(V1Container())
//        containers[0].name = genRepoName(repo)
//        containers[0].image = "alpine:3.13.2"
//        containers[0].command = listOf("sh", "-c", createCommand(repo, config))
//        containers[0].volumeMounts = listOf(volMount)
//        return containers
//    }
//
//    private fun createCommand(repo: Repository, config: Config): String =
//        listOf(
//            "apk add git rsync",
//            "git clone ${repo.url} repo",
//            "mkdir ${config.workdir}/repo",
//            "cd repo",
//            "rsync -a ./${repo.dir}/ ${config.workdir}/repo/",
//        ).joinToString(" && ")
//
//}
//
