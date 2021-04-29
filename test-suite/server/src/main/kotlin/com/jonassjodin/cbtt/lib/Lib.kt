package com.jonassjodin.cbtt.lib

import com.jonassjodin.cbtt.config.password
import com.jonassjodin.cbtt.config.username
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.response.*
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

fun hash(text: String): String {
    val bytes = text.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun getEnv(key: String, default: String) = System.getenv(key) ?: default

fun getEnv(key: String): String {
    val value = System.getenv(key)
    if (value.isEmpty()) {
        val cause = "Missing environment variable '$key'"
        println(cause)
        throw Exception(cause)
    }
    return value
}

suspend fun checkHTTPAuth(call: ApplicationCall): Unit? {
    val authHeader = call.request.parseAuthorizationHeader()
    if (!(authHeader == null || authHeader !is HttpAuthHeader.Single || authHeader.authScheme != "Basic")) {
        if (checkAuth(authHeader.blob) == null) {
            call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
            return null
        }
    } else {
        call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
        return null
    }
    return Unit
}

suspend fun checkWSAuth(call: ApplicationCall): Unit? {
    val auth = call.request.queryParameters["auth"]
    if (auth == null || auth.isEmpty()) {
        call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
        return null
    }
    if (checkAuth(auth) == null) {
        call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
        return null
    }
    return Unit
}

private fun checkAuth(credentials: String): Unit? {
    try {
        val decoded = String(Base64.getDecoder().decode(credentials), StandardCharsets.UTF_8)
        val splitted = decoded.split(":", limit = 2)
        if (splitted[0] != username || splitted[1] != password) {
            return null
        }
    } catch (e: Exception) {
        return null
    }
    return Unit
}