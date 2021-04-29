package com.jonassjodin.cbtt.k8s.test

import io.fabric8.kubernetes.client.KubernetesClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

object MyPodLogs {
    fun list(client: KubernetesClient, name: String, channel: Channel<String>) {
        val sb = StringBuilder()

        val logs = client.pods().withName(name).watchLog()
        val stream = logs.output
        val buff = BufferedReader(InputStreamReader(stream))
        var start = System.nanoTime()

        var line = buff.readLine()
        while (line != null) {
            sb.append(line)
            val current = System.nanoTime()

            if (current - start >= 100000000L) {
                val str = sb.toString()
                GlobalScope.launch { channel.send(str) }
                sb.setLength(0)
                start = System.nanoTime()
            }

            if (sb.isNotEmpty()) sb.append('\n')

            line = buff.readLine()
        }
        if (sb.isNotEmpty()) runBlocking { channel.send(sb.toString()) }
    }
}