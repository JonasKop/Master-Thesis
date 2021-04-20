package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.k8s.K8s
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

fun Route.logsRouting() {
    webSocket("/api/logs/{name}") {
        val name = call.parameters["name"] ?: return@webSocket call.respondText(
            "Missing name url parameter",
            status = HttpStatusCode.BadRequest
        )

        val channel = Channel<String>()
        GlobalScope.launch { K8s.listenToLogs(name, channel) }

        while (true) {
            val message = channel.receive()
            send(Frame.Text(message))
        }
    }
}