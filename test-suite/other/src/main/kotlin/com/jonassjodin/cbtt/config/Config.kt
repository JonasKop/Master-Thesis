@file:Suppress("UNCHECKED_CAST")

package com.jonassjodin.cbtt.config

import com.charleskorn.kaml.Yaml
import java.io.File
import java.io.FileNotFoundException

fun readFileAsString() = readFileAsString(configFile)
fun readFileAsString(configFile: String): String {
    try {
        return File(configFile).readText()
    } catch (e: FileNotFoundException) {
        println("Could not parse file '$configFile'")
        throw throw e
    }
}

fun readConfig(): Config {
    val fileContent = readFileAsString(configFile)
    val config = Yaml.default.decodeFromString(Config.serializer(), fileContent)
    validateConfig(config)
    return config
}

fun saveConfig(fileContent: String) {
    val config = Yaml.default.decodeFromString(Config.serializer(), fileContent)
    validateConfig(config)
    File(configFile).writeText(fileContent)
}
