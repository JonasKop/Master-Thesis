package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.config.readFileAsString
import com.jonassjodin.cbtt.config.saveConfig
import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.lib.checkHTTPAuth
import com.jonassjodin.cbtt.models.ConfigFile
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
            call.application.environment.log.error("GET /config")
            checkHTTPAuth(call) ?: return@get
            val config = readFileAsString()
            call.respond(ConfigFile(config))
        }

        put {
            call.application.environment.log.error("PUT /config")
            checkHTTPAuth(call) ?: return@put
            try {
                val configFile = call.receive<ConfigFile>()
                saveConfig(configFile.config)
                K8s.syncRepos()
                call.respond(Ok(true))
            } catch (e: Exception) {
                println(e)
                call.respondText("Invalid config", status = HttpStatusCode.BadRequest)
            }
        }
    }
}