package com.jonassjodin.cbtt.config

import com.jonassjodin.cbtt.lib.getEnv


val registryUrl = getEnv("REGISTRY_URL")
val registryPrefix = getEnv("REGISTRY_PREFIX")
val registryUsername = getEnv("REGISTRY_USERNAME")
val registryPassword = getEnv("REGISTRY_PASSWORD")
val grafanaUrl = getEnv("GRAFANA_URL")

val username = getEnv("USERNAME")
val password = getEnv("PASSWORD")

val configFile = getEnv("CONFIG_FILE", "config.yaml")