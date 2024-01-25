package com.khush.eventkt.event

import androidx.annotation.Keep
import java.util.UUID

/**
 * Data class defining single event for the library
 *
 * @property name Name of the event
 * @property parameters Parameters associated with the event
 * @property id Unique id associated with the event
 * @property status Status of the event at any point, Also check - [EventStatus]
 */
@Keep
data class Event(
    val name: String,
    val parameters: HashMap<String, Any> = hashMapOf(),
    val id: String = System.nanoTime().toString().plus(UUID.randomUUID().toString()),
    var status: String = EventStatus.DEFAULT.name
)
