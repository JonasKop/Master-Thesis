package com.jonassjodin.cbtt.worker

import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.k8s.test.Job
import io.fabric8.kubernetes.api.model.Pod
import java.util.concurrent.LinkedBlockingQueue

fun podStatus(pod: Pod): String {
    val s = pod.status?.containerStatuses?.map {
        val state = it.state
        when {
            state == null -> "Unknown"
            state.waiting != null -> state.waiting!!.reason
            state.running != null -> "Running"
            state.terminated != null -> state.terminated!!.reason
            else -> "Unknown"
        }
    } ?: return "Unknown"

    if (s.isEmpty()) return "Unknown"
    return s[0]!!
}

object Worker {
    private val queue = LinkedBlockingQueue<Job>()

    init {
        Thread { running() }.start()
    }

    fun addJob(job: Job) = queue.put(job)

//    private fun podIsNotCompleted(pod: Pod): Boolean =
//        pod.status?.containerStatuses?.any { it.state == null || it.state?.terminated == null } ?: true

    private fun isNotTerminated(pod: Pod): Boolean {
        val isTerminated = pod.status?.containerStatuses?.any { it.state?.terminated != null } ?: false
        return !isTerminated
    }


    private fun waitForAllCompleted() {
        while (true) {
            println("during")
            val jobs = K8s.listTestJobs()

            if (jobs.isNotEmpty()) {
                val notAllCompleted = jobs.any(::isNotTerminated)

                if (notAllCompleted) {
                    Thread.sleep(1000)
                    continue
                }
            }

            break
        }
    }

    private fun running() {
        while (true) {
            println("before")
            waitForAllCompleted()
            println("after")
            val job = queue.take()
            K8s.runJob(job)
        }
    }

    fun getQueue() = queue.toArray().toList().map { it as Job }
}
