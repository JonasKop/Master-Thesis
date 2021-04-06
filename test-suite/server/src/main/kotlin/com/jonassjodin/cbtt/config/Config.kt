@file:Suppress("UNCHECKED_CAST")

package com.jonassjodin.cbtt.config

import com.charleskorn.kaml.Yaml
import java.io.File
import java.io.FileNotFoundException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val configFile = System.getenv("CONFIG_FILE") ?: "config.yaml"
val gson = Gson()

fun <T> T.serializeToMap(): Map<String, Any?> = convert()

inline fun <reified T> Map<String, Any?>.toDataClass(): T = convert()

inline fun <I, reified O> I.convert(): O {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<O>() {}.type)
}

fun readFile(): Config = readFile(configFile)

fun readFile(configFile: String): Config {
    try {
        val fileContent = File(configFile).readText()
        return Yaml.default.decodeFromString(Config.serializer(), fileContent)
    } catch (e: FileNotFoundException) {
        println("Could not parse file '$configFile'")
        throw throw e
    }
}

fun readConfig(): Config {
    val config = readFile(configFile)
    validateConfig(config)
    return config;
}

fun saveConfig(config: Config) {
    val fileContent = Yaml.default.encodeToString(Config.serializer(), config)
    File(configFile).writeText(fileContent)
}
