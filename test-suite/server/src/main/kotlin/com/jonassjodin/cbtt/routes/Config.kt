package com.jonassjodin.cbtt.routes

import com.charleskorn.kaml.Yaml
import com.jonassjodin.cbtt.config.Config
import com.jonassjodin.cbtt.config.readFileAsString
import com.jonassjodin.cbtt.config.saveConfig
import com.jonassjodin.cbtt.config.validateConfig
import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.models.ConfigFile
import com.jonassjodin.cbtt.models.Ok
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception

fun Route.configRouting() {
    route("/api/config") {
        get {
            val config = readFileAsString()
            call.respond(ConfigFile(config))
        }

        put {
            try {
                val configFile = call.receive<ConfigFile>()
                val config = Yaml.default.decodeFromString(Config.serializer(), configFile.config)
                validateConfig(config)
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