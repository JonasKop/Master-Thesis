package com.jonassjodin.cbtt.k8s.test

import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod

object TestJobs {

    fun list(namespace: String): List<V1Pod> {
        val api = CoreV1Api()
        val jobs = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null)
        return jobs.items.filter { it.metadata?.name?.startsWith("cbtt-test") == true }
    }
}