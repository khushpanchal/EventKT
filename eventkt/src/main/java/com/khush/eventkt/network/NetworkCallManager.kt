package com.khush.eventkt.network

import com.khush.eventkt.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Network call manager: Whenever a batch of [Event] is ready, makes the network call
 *
 * @property apiUrl API Url for network call
 * @property apiHeaders API headers for network call
 */
internal class NetworkCallManager(
    private val apiUrl: String,
    private val apiHeaders: HashMap<String, String>
) : IGroupEventListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onEventGrouped(
        events: List<Event>,
        jsonString: String,
        networkSuccess: (Boolean, String, List<Event>) -> Unit
    ) {
        scope.launch {
            NetworkUtil.performNetworkOperation(apiUrl, apiHeaders, jsonString) {
                networkSuccess(it, jsonString, events)
            }
        }
    }
}