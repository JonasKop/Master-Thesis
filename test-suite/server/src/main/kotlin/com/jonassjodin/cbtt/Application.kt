package com.jonassjodin.cbtt

import com.charleskorn.kaml.Yaml
import com.jonassjodin.cbtt.config.Config
import com.jonassjodin.cbtt.config.readConfig
import com.jonassjodin.cbtt.k8s.test.Job
import com.jonassjodin.cbtt.routes.registerConfigRoutes
import com.jonassjodin.cbtt.worker.Worker
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*

fun main(args: Array<String>) {
    val conf = readConfig()
    println(Yaml.default.encodeToString(Config.serializer(), conf))
//    K8s.syncRepos()
    val job = Job(conf.repositories[0], conf.buildTools[4])
    val w = Worker()
    w.addJob(job)
    w.run()

//    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }
    registerConfigRoutes()
}

