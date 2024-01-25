package com.khush.eventkt.event

import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.khush.eventkt.event.EventStatus.DEFAULT
import com.khush.eventkt.event.EventStatus.FAILED
import com.khush.eventkt.event.EventStatus.PENDING
import com.khush.eventkt.event.EventStatus.SUCCESS
import com.khush.eventkt.network.IGroupEventListener
import com.khush.eventkt.network.model.EventsRequestBody
import com.khush.eventkt.network.model.RequestBody
import com.khush.eventkt.observer.AppLifecycleObserver
import com.khush.eventkt.persistent.ICacheScheme
import com.khush.eventkt.utils.Utils.toJson

internal class EventManager(
    private val eventNumThreshold: Int,
    private val iGroupEventListener: IGroupEventListener,
    private val iCacheScheme: ICacheScheme,
) {

    private var currentEventNum = iCacheScheme.getEventSize()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(this))
    }

    fun add(event: Event, force: Boolean = false) {
        Log.i("EventKtLogs", "Event Added: ${event.toJson()}")
        iCacheScheme.add(event)
        if (checkThreshold() || force) {
            flushAll()
        }
    }

    private fun checkThreshold(): Boolean {
        currentEventNum = iCacheScheme.getEventSize(listOf(DEFAULT))
        return (currentEventNum >= eventNumThreshold)
    }

    @Synchronized
    fun flushAll() { //cleaning storage, status, network call
        iCacheScheme.removeAll(listOf(SUCCESS))
        performNetworkRequest()
    }

    private fun networkSuccess(eventList: List<Event>) {
        iCacheScheme.updateEventStatusAll(eventList, SUCCESS)
        iCacheScheme.removeAll(listOf(SUCCESS))
    }

    private fun networkFailure(eventList: List<Event>) {
        iCacheScheme.updateEventStatusAll(eventList, FAILED)
    }

    private fun performNetworkRequest() {
        val eventList = iCacheScheme.getEvents(listOf(DEFAULT, FAILED))
        if (eventList.isEmpty()) return
        iCacheScheme.updateEventStatusAll(eventList, PENDING)
        val jsonStringRequest = eventListToJsonString(eventList)
        Log.i("EventKtLogs", "Network call started with request body: $jsonStringRequest")
        iGroupEventListener.onEventGrouped(
            eventList,
            jsonStringRequest
        ) { success, requestBody, list ->
            if (success) {
                Log.i("EventKtLogs", "Network call success with request body: $requestBody")
                networkSuccess(list)
            } else {
                Log.i("EventKtLogs", "Network call failed with request body: $requestBody")
                networkFailure(list)
            }
        }
    }

    private fun eventListToJsonString(eventList: List<Event>): String {
        val eventRequestBody = mutableListOf<EventsRequestBody>()
        eventList.forEach {
            val map = HashMap<String, Any>()
            it.parameters.forEach { param ->
                map[param.key] = param.value
            }
            eventRequestBody.add(EventsRequestBody(it.name, map))
        }
        val requestBody = RequestBody(eventRequestBody.toList())
        return requestBody.toJson()
    }

}