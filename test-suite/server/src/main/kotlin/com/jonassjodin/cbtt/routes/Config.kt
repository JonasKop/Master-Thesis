package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.config.*
import com.jonassjodin.cbtt.models.Ok
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception

fun Route.configRouting() {
    route("/config") {
        get {
            val config = readFile()
            call.respond(config)
        }

        put {
            try {
                val config = call.receive<Config>()
                validateConfig(config)
                saveConfig(config)
                call.respond(Ok(true))
            } catch (e: Exception) {
                call.respondText("Invalid config", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

fun Application.registerConfigRoutes() {
    routing {
        configRouting()
    }
}