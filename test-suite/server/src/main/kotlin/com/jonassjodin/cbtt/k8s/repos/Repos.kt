package com.jonassjodin.cbtt.k8s.repos

import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.config.readConfig
import io.kubernetes.client.openapi.apis.BatchV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.*

class Repos(private val namespace: String) {
    private val config = readConfig()

    fun syncRepos() {
        val pvcs = getPVCs()
        val jobs = getJobs()
        val pvcsInfo = pvcs.map { nameAndChecksum(it) }
        val jobsInfo = jobs.map { nameAndChecksum(it) }

        val newPvcs = config.repositories
        val newJobs = config.repositories
        val newPvcsInfo = newPvcs.map { nameAndChecksum(RepoPVC(it, namespace).k8sPvc) }
        val newJobsInfo = newJobs.map { nameAndChecksum(RepoJob(config, it, namespace).k8sJob) }

        if (pvcsInfo.containsAll(newPvcsInfo) && jobsInfo.containsAll(newJobsInfo)) {
            return
        }
        pvcsInfo.forEach { deleteRepo(it.first) }
        config.repositories.forEach(::createRepo)
    }

    private fun getPVCs(): MutableList<V1PersistentVolumeClaim> {
        val api = CoreV1Api()
        val pvcs =
            api.listNamespacedPersistentVolumeClaim(
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
        return pvcs.items
    }

    private fun getJobs(): MutableList<V1Job> {
        val api = BatchV1Api()
        val jobs = api.listNamespacedJob(namespace, null, null, null, null, null, null, null, null, null, null)
        return jobs.items
    }

    private fun deleteRepo(name: String) {
        val jobApi = BatchV1Api()
        jobApi.deleteNamespacedJob(name, namespace, null, null, null, null, "Foreground", null)
        val coreApi = CoreV1Api()
        coreApi.deleteNamespacedPersistentVolumeClaim(name, namespace, null, null, null, null, null, null)
    }

    private fun createRepo(repo: Repository) {
        val pvc = RepoPVC(repo, namespace)
        val job = RepoJob(config, repo, namespace)
        pvc.apply()
        job.apply()
    }
}
