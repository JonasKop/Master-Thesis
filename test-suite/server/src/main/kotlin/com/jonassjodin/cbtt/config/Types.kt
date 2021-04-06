package com.jonassjodin.cbtt.config

import kotlinx.serialization.Serializable

enum class BuildSystem {
    gradle
}

@Serializable
data class Repository(
    val name: String,
    val url: String,
    val dir: String,
    val jib: Boolean = false,
)

@Serializable
data class SecurityContext(
    val seccomp: String? = null,
    val apparmor: String? = null,
    val privileged: Boolean? = null,
    val userID: Int? = null
)

@Serializable
data class BuildTool(
    val name: String,
    val image: String,
    val command: List<String>,
    val securityContext: SecurityContext? = null,
    val env: Map<String, String>? = null
)

@Serializable
data class Config(
    val workdir: String,
    val repositories: List<Repository>,
    val buildTools: List<BuildTool>
)
