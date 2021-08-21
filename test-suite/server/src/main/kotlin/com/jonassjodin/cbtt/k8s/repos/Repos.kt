package com.jonassjodin.cbtt.k8s.repos

import LocalCache
import com.jonassjodin.cbtt.config.Repository
import com.jonassjodin.cbtt.config.readConfig
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient


class Repos {
    private val config = readConfig()

    fun syncRepos(client: KubernetesClient) {
        val pvcs = getRepoPVCs(client)
        val jobs = getRepoPods(client)

        val pvcsInfo = pvcs.map { nameAndChecksum(it) }
        val jobsInfo = jobs.map { nameAndChecksum(it) }

        val newPvcs = config.repositories
        val newJobs = config.repositories
        val newPvcsInfo = newPvcs.map { nameAndChecksum(it, config.workdir) }
        val newJobsInfo = newJobs.map { nameAndChecksum(it, config.workdir) }

        if (pvcsInfo.containsAll(newPvcsInfo) && jobsInfo.containsAll(newJobsInfo)) return

        jobsInfo.forEach { deletePod(it.first, client) }
        pvcsInfo.forEach { deletePVC(it.first, client) }
        config.repositories.forEach { createRepo(it, client) }
    }

    fun syncLocalCache(client: KubernetesClient) {
        val new = config.buildTools.filter { it.localCacheDir != null }.map { it.name }
        val old = getCachePVCs(client).map { it.metadata.name }

        if (!old.containsAll(new) || !new.containsAll(old)) {
            getCachePVCs(client).forEach { deletePVC(it.metadata.name, client) }

            config.buildTools.filter { it.localCacheDir != null }.forEach {
                client.persistentVolumeClaims().create(LocalCache(it))
            }
        }
    }


    private fun getCachePVCs(client: KubernetesClient): List<PersistentVolumeClaim> {
        val pvcs = client.persistentVolumeClaims().list()

        return pvcs.items.filter { it.metadata?.name?.startsWith("cbtt-repo") == false }
    }

    private fun getRepoPVCs(client: KubernetesClient): List<PersistentVolumeClaim> {
        val pvcs = client.persistentVolumeClaims().list()
        return pvcs.items.filter { it.metadata?.name?.startsWith("cbtt-repo") == true }
    }

    fun getRepoPods(client: KubernetesClient): List<Pod> {
        val pods = client.pods().list()
        return pods.items.filter { it.metadata?.name?.startsWith("cbtt-repo") == true }
    }

    private fun deletePVC(name: String, client: KubernetesClient) {
        println("Deleting repository pvc $name")
        client.persistentVolumeClaims().withName(name).delete()
    }

    private fun deletePod(name: String, client: KubernetesClient) {
        println("Deleting repository pod $name")
        client.pods().withName(name).delete()
    }

    private fun createRepo(repo: Repository, client: KubernetesClient) {
        client.pods().create(RepoPod(repo))
        client.persistentVolumeClaims().create(RepoPVC(repo))
    }
}
