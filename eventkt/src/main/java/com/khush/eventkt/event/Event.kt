package com.khush.eventkt.event

import androidx.annotation.Keep
import java.util.UUID


@Keep
data class Event(
    val name: String,
    val parameters: HashMap<String, Any> = hashMapOf(),
    val id: String = System.nanoTime().toString().plus(UUID.randomUUID().toString()),
    var status: String = EventStatus.DEFAULT.name
)
