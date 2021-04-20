package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.k8s.K8s
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.podRouting() {
    route("/api/pod/{name}") {
        delete {
            val name = call.parameters["name"] ?: return@delete call.respondText(
                "Missing name url parameter",
                status = HttpStatusCode.BadRequest
            )
            K8s.deletePod(name)
        }
    }
}