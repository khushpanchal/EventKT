package com.khush.eventkt.event

import androidx.lifecycle.ProcessLifecycleOwner
import com.khush.eventkt.Logger
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


internal class EventManager(
    private val numBased: Boolean,
    private val eventNumThreshold: Int,
    private val timeBased: Boolean,
    private val eventTimeThreshold: Long,
    private val sizeBased: Boolean,
    private val eventSizeThreshold: Int,
    private val iGroupEventListener: IGroupEventListener,
    private val iCacheScheme: ICacheScheme,
    private val logger: Logger
) {

    private var currentEventNum = iCacheScheme.getEventSize()
    private var timerJob: Job? = null
    private var currentEventSize = iCacheScheme.getEventSizeInBytes()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(this))
        iCacheScheme.syncDataFromCache { success, list ->
            if (success) {
                iCacheScheme.getEvents(listOf(PENDING)).forEach { event ->
                    if (event in list) {
                        iCacheScheme.updateEventStatus(event, FAILED)
                    }
                }
                flushAll()
            }
        }
    }


    fun add(event: Event, force: Boolean = false) {
        logger.log(msg = "Event Added: ${event.toJson()}")
        iCacheScheme.add(event)
        if (checkThreshold() || force) {
            flushAll()
        }
    }


    private fun checkThreshold(): Boolean {
        currentEventNum = iCacheScheme.getEventSize(listOf(DEFAULT))
        currentEventSize = iCacheScheme.getEventSizeInBytes(listOf(DEFAULT))
        return (numBased && currentEventNum >= eventNumThreshold)
                || (sizeBased && currentEventSize >= eventSizeThreshold)
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
        logger.log(msg = "Network call started with request body: $jsonStringRequest")
        iGroupEventListener.onEventGrouped(
            eventList,
            jsonStringRequest
        ) { success, requestBody, list ->
            if (success) {
                logger.log(msg = "Network call success for request body: $requestBody")
                networkSuccess(list)
            } else {
                logger.log(msg = "Network call failed for request body: $requestBody")
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


    fun startTimer() {
        if (!timeBased) return
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(eventTimeThreshold)
                flushAll()
            }
        }
    }


    fun stopTimer() {
        if (!timeBased) return
        timerJob?.cancel()
    }

}