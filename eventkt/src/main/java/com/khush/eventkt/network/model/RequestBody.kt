package com.khush.eventkt.network.model

import androidx.annotation.Keep
import java.io.Serializable


@Keep
internal data class RequestBody(
    val events: List<EventsRequestBody>
) : Serializable