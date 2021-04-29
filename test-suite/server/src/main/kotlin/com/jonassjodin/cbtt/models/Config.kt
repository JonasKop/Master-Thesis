package com.jonassjodin.cbtt.models

import com.jonassjodin.cbtt.k8s.test.Job
import kotlinx.serialization.Serializable

@Serializable
data class Ok(val ok: Boolean)

@Serializable
data class Status(val pods: List<Pod>, val queue: List<Job>, val grafanaUrl: String)

@Serializable
data class Pod(val name: String, val status: String)

@Serializable
data class ConfigFile(val config: String)

@Serializable
data class IncomingJob(val repoName: String, val buildToolName: String, val cache: Boolean, val push: Boolean)

@Serializable
data class Credentials(val username: String, val password: String)