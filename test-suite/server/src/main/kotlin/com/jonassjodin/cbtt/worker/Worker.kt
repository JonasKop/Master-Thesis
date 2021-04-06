package com.jonassjodin.cbtt.worker

import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.k8s.test.Job
import java.util.concurrent.LinkedBlockingQueue

class Worker {
    private val queue = LinkedBlockingQueue<Job>()

    fun addJob(job: Job) = queue.put(job)

    fun run() {
        Thread {
            while (true) {
                val s = queue.take()
                K8s.runJob(s)
                println("tester")
            }
        }.start()
    }
}
