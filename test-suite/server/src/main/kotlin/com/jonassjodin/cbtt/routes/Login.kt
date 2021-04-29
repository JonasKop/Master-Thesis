package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.config.password
import com.jonassjodin.cbtt.config.username
import com.jonassjodin.cbtt.models.Credentials
import com.jonassjodin.cbtt.models.Ok
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception

fun Route.loginRouting() {
    route("/login") {
        post {
            call.application.environment.log.error("POST /login")
            try {
                val credentials = call.receive<Credentials>()
                if (credentials.username != username || credentials.password != password) {
                    call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
                } else {
                    call.respond(Ok(true))
                }
            } catch (e: Exception) {
                println(e)
                call.respondText("Invalid config", status = HttpStatusCode.BadRequest)
            }
        }
    }
}
