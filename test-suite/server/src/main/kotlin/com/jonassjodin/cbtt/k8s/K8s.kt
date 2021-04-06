package com.jonassjodin.cbtt.k8s

import com.jonassjodin.cbtt.k8s.repos.Repos
import com.jonassjodin.cbtt.k8s.test.Job
import com.jonassjodin.cbtt.k8s.test.Test
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config as K8sConfig
import java.io.File
import java.io.FileNotFoundException

object K8s {
    private val namespace = getRunningNamespace()

    fun syncRepos() = Repos(namespace).syncRepos()

    fun runJob(job: Job) = Test(job, namespace).apply()

    init {
        val client: ApiClient = K8sConfig.defaultClient()
//        client.isDebugging = true
        Configuration.setDefaultApiClient(client)
    }

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