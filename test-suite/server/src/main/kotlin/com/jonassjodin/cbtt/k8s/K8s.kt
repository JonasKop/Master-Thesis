package com.jonassjodin.cbtt.k8s

import com.jonassjodin.cbtt.k8s.repos.Repos
import com.jonassjodin.cbtt.k8s.test.*
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import kotlinx.coroutines.channels.Channel
import io.kubernetes.client.util.Config as K8sConfig
import java.io.File
import java.io.FileNotFoundException

object K8s {
    private val namespace = getRunningNamespace()

    init {
        val client: ApiClient = K8sConfig.defaultClient()
        client.isDebugging = true
        Configuration.setDefaultApiClient(client)
    }

    fun deletePod(name: String) = Pods.delete(name, namespace)

    fun listPods() = Pods.list(namespace)

    fun listenToLogs(name: String, channel: Channel<String>) = MyPodLogs.list(namespace, name, channel)

    fun listRepos() = Repos.getPods(namespace)

    fun syncRepos() = Repos.syncRepos(namespace)

    fun listTestJobs() = TestJobs.list(namespace)

    fun runJob(job: Job) = Test.apply(job, namespace)

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