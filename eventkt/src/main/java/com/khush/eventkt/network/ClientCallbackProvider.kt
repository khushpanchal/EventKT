package com.khush.eventkt.network

import com.khush.eventkt.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


typealias EventNameParam = Pair<String, HashMap<String, Any>>


internal class ClientCallbackProvider(private val makeNetworkRequest: suspend (String, List<EventNameParam>) -> Boolean) :
    IGroupEventListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onEventGrouped(
        events: List<Event>,
        jsonString: String,
        networkSuccess: (Boolean, String, List<Event>) -> Unit
    ) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val success = makeNetworkRequest.invoke(jsonString,
                    events.map {
                        EventNameParam(it.name, it.parameters)
                    }
                )
                withContext(Dispatchers.Main) {
                    networkSuccess(success, jsonString, events)
                }
            }
        }
    }
}