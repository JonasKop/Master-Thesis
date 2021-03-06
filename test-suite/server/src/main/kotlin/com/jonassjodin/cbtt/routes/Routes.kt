package com.jonassjodin.cbtt.routes

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*

fun Application.registerRoutes() {
    routing {
        configRouting()
        podRouting()
        testRouting()
        logsRouting()
        listStateRouting()
        repoRouting()
        loginRouting()
    }
}