package com.khush.eventkt.network.model

import androidx.annotation.Keep
import java.io.Serializable

/**
 * Request body for API call
 *
 * @property events List of [EventsRequestBody]
 */
@Keep
internal data class RequestBody(
    val events: List<EventsRequestBody>
) : Serializable