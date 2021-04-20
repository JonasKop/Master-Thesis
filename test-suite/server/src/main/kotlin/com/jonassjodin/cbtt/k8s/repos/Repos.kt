package com.jonassjodin.cbtt.k8s.repos

import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.config.readConfig
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*

object Repos {
    private val config = readConfig()

    fun syncRepos(namespace: String) {
        val pvcs = getPVCs(namespace)
        val jobs = getPods(namespace)
        val pvcsInfo = pvcs.map { nameAndChecksum(it) }
        println(pvcsInfo)
        val jobsInfo = jobs.map { nameAndChecksum(it) }

        val newPvcs = config.repositories
        val newJobs = config.repositories
        val newPvcsInfo = newPvcs.map(::nameAndChecksum)
        val newJobsInfo = newJobs.map(::nameAndChecksum)

        if (pvcsInfo.containsAll(newPvcsInfo) && jobsInfo.containsAll(newJobsInfo)) return

        jobsInfo.forEach { deletePod(it.first, namespace) }
        pvcsInfo.forEach { deletePVC(it.first, namespace) }
        config.repositories.forEach { createRepo(it, namespace) }
    }

    private fun getPVCs(namespace: String): List<V1PersistentVolumeClaim> {
        val api = CoreV1Api()
        val pvcs = api.listNamespacedPersistentVolumeClaim(
            namespace,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        return pvcs.items.filter { it.metadata?.name?.startsWith("cbtt-repo") == true }
    }

    fun getPods(namespace: String): List<V1Pod> {
        val api = CoreV1Api()
        val pods = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null)
        return pods.items.filter { it.metadata?.name?.startsWith("cbtt-repo") == true }
    }


    private fun deletePVC(name: String, namespace: String) {
        println("Deleting repository pvc $name")
        val coreApi = CoreV1Api()
        coreApi.deleteNamespacedPersistentVolumeClaim(name, namespace, null, null, null, null, null, null)
    }

    private fun deletePod(name: String, namespace: String) {
        println("Deleting repository pod $name")
        val coreApi = CoreV1Api()
        coreApi.deleteNamespacedPod(name, namespace, null, null, null, null, "Foreground", null)
    }

    private fun createRepo(repo: Repository, namespace: String) {
        RepoPVC.apply(repo, namespace)
        RepoPod.apply(config, repo, namespace)
    }
}
