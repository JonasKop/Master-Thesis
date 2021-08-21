package com.jonassjodin.cbtt.routes

import io.ktor.http.content.*
import io.ktor.routing.*

fun Route.staticRouting() {
    files("static")
}