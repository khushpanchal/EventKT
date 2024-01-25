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

/**
 * Event manager: Manages persistent and network operations
 *
 * - Checks threshold
 * - Flush events to network
 * - Manages status update of events
 * - Creates API request
 * - Responsible for logging on every every event add and successful or failure network calls
 *
 * @property numBased Boolean to decide if [flushAll] happens based on event counts
 * @property eventNumThreshold Maximum number of events after which [flushAll] gets called
 * @property timeBased Boolean to decide if [flushAll] happens based on time
 * @property eventTimeThreshold Time interval in milliseconds at which [flushAll] gets called
 * @property sizeBased Boolean to decide if [flushAll] happens based on events size
 * @property eventSizeThreshold Maximum size of events in bytes after which [flushAll] gets called
 * @property iGroupEventListener Decides whether network call made by client or library
 * @property iCacheScheme Decides the Caching logic, class responsible for storing events
 * @property logger Logging events
 */
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

    /**
     * - Add event to storage
     * - Check threshold [checkThreshold]
     * - Flush all the events [flushAll]
     *
     * @param event Single [Event]
     * @param force Boolean if true, all events in storage gets flushed immediately
     */
    fun add(event: Event, force: Boolean = false) {
        logger.log(msg = "Event Added: ${event.toJson()}")
        iCacheScheme.add(event)
        if (checkThreshold() || force) {
            flushAll()
        }
    }

    /**
     * Check threshold passed to [EventManager]
     *
     * @return Boolean for valid threshold
     */
    private fun checkThreshold(): Boolean {
        currentEventNum = iCacheScheme.getEventSize(listOf(DEFAULT))
        currentEventSize = iCacheScheme.getEventSizeInBytes(listOf(DEFAULT))
        return (numBased && currentEventNum >= eventNumThreshold)
                || (sizeBased && currentEventSize >= eventSizeThreshold)
    }

    /**
     * Flush all: Remove all the [SUCCESS] status event from storage and [performNetworkRequest]
     *
     */
    @Synchronized
    fun flushAll() { //cleaning storage, status, network call
        iCacheScheme.removeAll(listOf(SUCCESS))
        performNetworkRequest()
    }

    /**
     * On Network success marked the events status as [SUCCESS]
     *
     * @param eventList List of [Event]
     */
    private fun networkSuccess(eventList: List<Event>) {
        iCacheScheme.updateEventStatusAll(eventList, SUCCESS)
        iCacheScheme.removeAll(listOf(SUCCESS))
    }

    /**
     * On Network failure marked the events status as [FAILED]
     *
     * @param eventList List of [Event]
     */
    private fun networkFailure(eventList: List<Event>) {
        iCacheScheme.updateEventStatusAll(eventList, FAILED)
    }

    /**
     * Fetch all [DEFAULT] and [FAILED] events and marked them [PENDING]
     *
     * Start network request by calling [IGroupEventListener.onEventGrouped] and gets success status and [Event] list
     *
     */
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

    /**
     * Takes the list of event and converts to Json String for API request body
     *
     * @param eventList List of [Event]
     * @return Request body as json string
     */
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

    /**
     * Start [timerJob] for time based batching of event and [flushAll] every [eventTimeThreshold] milliseconds
     *
     */
    fun startTimer() {
        if (!timeBased) return
        timerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(eventTimeThreshold)
                flushAll()
            }
        }
    }

    /**
     * Cancel [timerJob] for time based batching of events
     *
     */
    fun stopTimer() {
        if (!timeBased) return
        timerJob?.cancel()
    }

}