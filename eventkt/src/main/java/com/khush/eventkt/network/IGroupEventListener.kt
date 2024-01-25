package com.khush.eventkt.network

import com.khush.eventkt.event.Event

/**
 * Interface implemented by [ClientCallbackProvider] and [NetworkCallManager]
 *
 */
internal interface IGroupEventListener {
    /**
     * Invoked whenever a batch of events get ready for network call
     *
     * @param events List of [Event]
     * @param jsonString Generated Request body to be sent with API call
     * @param networkSuccess Lambda (true/false for network success, jsonString, list of [Event])
     * get invoked on success or failure of network call
     */
    fun onEventGrouped(
        events: List<Event>,
        jsonString: String,
        networkSuccess: (Boolean, String, List<Event>) -> Unit
    )
}