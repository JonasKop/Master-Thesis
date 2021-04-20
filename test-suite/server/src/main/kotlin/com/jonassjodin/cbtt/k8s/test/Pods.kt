package com.jonassjodin.cbtt.k8s.test

import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod

object Pods {
    fun list(namespace: String): List<V1Pod> {
        val api = CoreV1Api()
        val pods = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null)
        return pods.items
    }

    fun delete(name: String, namespace: String) {
        val api = CoreV1Api()
        api.deleteNamespacedPod(name, namespace, null, null, null, null, null, null)
    }
}