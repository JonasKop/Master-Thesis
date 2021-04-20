package com.jonassjodin.cbtt.lib

import java.security.MessageDigest

fun hash(text: String): String {
    val bytes = text.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun getEnv(key: String): String {
    val value = System.getenv(key)
    if (value.isEmpty()) throw Exception("Missing environment variable '$key'")
    return value
}

