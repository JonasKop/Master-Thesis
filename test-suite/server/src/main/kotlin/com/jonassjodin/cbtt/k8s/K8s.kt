package com.jonassjodin.cbtt.k8s

import com.jonassjodin.cbtt.k8s.repos.Repos
import com.jonassjodin.cbtt.k8s.test.*
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.ConfigBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileNotFoundException

object K8s {
    private val namespace = getRunningNamespace()
    private val client = DefaultKubernetesClient().inNamespace(namespace)

    fun deletePod(name: String): Boolean = client.pods().withName(name).delete()

    fun deletePVC(name: String): Boolean = client.persistentVolumeClaims().withName(name).delete()

    fun listPods(): List<Pod> = client.pods().list().items

    fun listenToLogs(name: String, channel: Channel<String>) = MyPodLogs.list(client, name, channel)

    fun listRepos() = Repos().getPods(client)

    fun syncRepos() = Repos().syncRepos(client)

    fun listTestJobs() = client.pods().list().items.filter { it.metadata?.name?.startsWith("cbtt-test") == true }

    fun runJob(job: Job): Pod = client.pods().create(Test(job))

    private fun getRunningNamespace(): String {
        val configFile = "/var/run/secrets/kubernetes.io/serviceaccount/namespace"
        try {
            return File(configFile).readText()
        } catch (e: FileNotFoundException) {
            println("Not running in kubernetes, using 'default' namespace")
        }
        return "default"
    }
}