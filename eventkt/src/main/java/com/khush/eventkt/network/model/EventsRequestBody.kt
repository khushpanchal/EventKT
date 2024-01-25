package com.khush.eventkt.network.model

import androidx.annotation.Keep

/**
 * Events request body
 *
 * @property event Name of the event
 * @property parameters Parameters associated with the event
 */
@Keep
internal data class EventsRequestBody(
    val event: String,
    val parameters: Map<String, Any>? = null
)
