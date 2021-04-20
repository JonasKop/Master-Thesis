@file:Suppress("UNCHECKED_CAST")

package com.jonassjodin.cbtt.config

import com.charleskorn.kaml.Yaml
import java.io.File
import java.io.FileNotFoundException

val configFile = System.getenv("CONFIG_FILE") ?: "config.yaml"

fun readFileAsString() = readFileAsString(configFile)
fun readFileAsString(configFile: String): String {
    try {
        return File(configFile).readText()
    } catch (e: FileNotFoundException) {
        println("Could not parse file '$configFile'")
        throw throw e
    }
}

fun readFile(configFile: String): Config {
    val fileContent = readFileAsString(configFile)
    return Yaml.default.decodeFromString(Config.serializer(), fileContent)
}

fun readConfig(): Config {
    val config = readFile(configFile)
    validateConfig(config)
    return config;
}

fun saveConfig(fileContent: String) {
    File(configFile).writeText(fileContent)
}
