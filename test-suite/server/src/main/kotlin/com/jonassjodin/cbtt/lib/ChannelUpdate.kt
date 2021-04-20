package com.jonassjodin.cbtt.lib

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ChannelUpdater<T>(val fn: () -> T, private val interval: Long) {
    private val li: MutableList<Channel<T>> = Collections.synchronizedList(ArrayList())

    init {
        GlobalScope.launch {
            while (true) {
                delay(interval)
                li.toList().forEach { it.send(fn()) }
            }
        }
    }

    fun removeChannel(c: Channel<T>) = li.remove(c)

    fun newChannel(): Channel<T> {
        val channel = Channel<T>()
        li.add(channel)
        return channel
    }
}