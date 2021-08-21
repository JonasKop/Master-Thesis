package com.jonassjodin.cbtt.config

import kotlinx.serialization.Serializable

@Serializable
data class Repository(
    val name: String,
    val url: String,
    val dir: String = "/",
    val tags: List<String>? = null,
)

@Serializable
data class SecurityContext(
    val privileged: Boolean? = null,
    val runAsUser: Long? = null,

    val seccomp: String? = null,
    val apparmor: String? = null,
)

@Serializable
data class Command(
    val push: String? = null,
    val noPush: String? = null
)

@Serializable
data class CommandTypes(
    val setup: String,
    val localCache: Command? = null,
    val remoteCache: Command? = null,
    val noCache: Command? = null
)

@Serializable
data class BuildTool(
    val name: String,
    val image: String,
    val tag: String? = null,
    val command: CommandTypes,
    val securityContext: SecurityContext? = null,
    val env: Map<String, String>? = null,
    val localCacheDir: String? = null
)

@Serializable
data class Limit(
    val memory: String,
    val cpu: String
)

@Serializable
data class ResourceRequirements(
    val limits: Limit,
    val requests: Limit
)

@Serializable
data class Config(
    val resources: ResourceRequirements,
    val workdir: String,
    val repositories: List<Repository>,
    val buildTools: List<BuildTool>
)
