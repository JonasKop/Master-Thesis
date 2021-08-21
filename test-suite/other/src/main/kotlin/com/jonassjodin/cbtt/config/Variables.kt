package com.jonassjodin.cbtt.config

import com.jonassjodin.cbtt.lib.getEnv


val registryUrl = getEnv("REGISTRY_URL")
val registryPrefix = getEnv("REGISTRY_PREFIX")
val registryUsername = getEnv("REGISTRY_USERNAME")
val registryPassword = getEnv("REGISTRY_PASSWORD")

val configFile = getEnv("CONFIG_FILE", "config.yaml")