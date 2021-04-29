package com.jonassjodin.cbtt.routes

import com.jonassjodin.cbtt.config.readConfig
import com.jonassjodin.cbtt.k8s.test.Job
import com.jonassjodin.cbtt.lib.checkHTTPAuth
import com.jonassjodin.cbtt.models.IncomingJob
import com.jonassjodin.cbtt.worker.Worker
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.lang.Exception

fun Route.testRouting() {
    route("/test") {
        post {
            call.application.environment.log.error("POST /test")
            checkHTTPAuth(call) ?: return@post
            try {
                val incomingJob = call.receive<IncomingJob>()
                val conf = readConfig()

                val repo = conf.repositories.find { it.name == incomingJob.repoName }
                val buildTool = conf.buildTools.find { it.name == incomingJob.buildToolName }
                if (repo == null) {
                    call.respondText("Invalid repo name", status = HttpStatusCode.BadRequest)
                    return@post
                }
                if (buildTool == null) {
                    call.respondText("Invalid build tool name", status = HttpStatusCode.BadRequest)
                    return@post
                }

                val job = Job(repo, buildTool, incomingJob.cache, incomingJob.push)
                Worker.addJob(job)
                call.respond(Worker.getQueue())
            } catch (e: Exception) {
                println(e)
                e.printStackTrace()
                call.respondText("Invalid job", status = HttpStatusCode.BadRequest)
            }
        }
    }
}