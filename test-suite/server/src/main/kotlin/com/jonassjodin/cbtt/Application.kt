package com.jonassjodin.cbtt

import com.jonassjodin.cbtt.config.readConfig
import com.jonassjodin.cbtt.k8s.K8s
import com.jonassjodin.cbtt.routes.registerRoutes
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.websocket.*

fun main(args: Array<String>) {
    readConfig()
    K8s.syncConfig()
    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") //
// Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets)

    registerRoutes()
}

