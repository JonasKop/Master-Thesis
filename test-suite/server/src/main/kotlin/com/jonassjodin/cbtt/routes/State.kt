package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.config.grafanaUrl
import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.lib.ChannelUpdater
import com.jonassjodin.cbtt.lib.checkWSAuth
import com.jonassjodin.cbtt.models.Pod
import com.jonassjodin.cbtt.models.Status
import com.jonassjodin.cbtt.worker.Worker
import com.jonassjodin.cbtt.worker.podStatus
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.concurrent.CancellationException

private val channelUpdater = ChannelUpdater(::listPods, 1000)

fun listPods(): List<Pod> {
    val pods = K8s.listPods()
    return pods.map { Pod(it.metadata!!.name!!, podStatus(it)) }
}

fun Route.listStateRouting() {
    webSocket("/state") {
        call.application.environment.log.error("WS /state")
        checkWSAuth(call) ?: return@webSocket
        val channel = channelUpdater.newChannel()
        while (true) {
            try {
                val message = channel.receive()
                val queue = Worker.getQueue()
                val status = Status(message, queue, grafanaUrl)
                val encoded = Json.encodeToString(Status.serializer(), status)
                send(Frame.Text(encoded))
            } catch (e: CancellationException) {
                channelUpdater.removeChannel(channel)
                break
            }
        }
    }
}