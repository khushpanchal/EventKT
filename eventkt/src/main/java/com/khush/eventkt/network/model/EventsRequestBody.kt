package com.khush.eventkt.network.model

import androidx.annotation.Keep


@Keep
internal data class EventsRequestBody(
    val event: String,
    val parameters: Map<String, Any>? = null
)
