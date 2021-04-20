package com.jonassjodin.cbtt.k8s.test

import io.kubernetes.client.PodLogs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

object MyPodLogs {
    fun list(namespace: String, name: String, channel: Channel<String>) {
        val sb = StringBuilder()
        val podLogs = PodLogs()
        val stream = podLogs.streamNamespacedPodLog(namespace, name, null)
        val buff = BufferedReader(InputStreamReader(stream))
        var start = System.nanoTime()

        var line = buff.readLine()
        while (line != null) {
            sb.append(line)
            val current = System.nanoTime()

            if (current - start >= 1000000000L) {
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