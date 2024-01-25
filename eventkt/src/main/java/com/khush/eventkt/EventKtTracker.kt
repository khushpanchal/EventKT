package com.khush.eventkt

import android.app.Application
import android.content.Context
import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventManager
import com.khush.eventkt.network.NetworkCallManager
import com.khush.eventkt.observer.ActivityLifecycleCallback
import com.khush.eventkt.persistent.ICacheScheme
import com.khush.eventkt.persistent.InMemoryCacheManager
import com.khush.eventkt.utils.Utils
import com.khush.eventkt.utils.Utils.validateThresholds

class EventKtTracker private constructor(
    context: Context,
    apiUrl: String,
    apiHeaders: HashMap<String, String>,
    eventNumThreshold: Int,
    eventTimeThreshold: Long,
    eventSizeThreshold: Int,
    cacheScheme: ICacheScheme,
    private val eventValidationConfig: EventValidationConfig
) {

    private val eventManager = EventManager(
        numBased = eventNumThreshold > 0, //if greater than 0 then count based
        eventNumThreshold = eventNumThreshold,
        timeBased = eventTimeThreshold > 0, //if greater than 0 then time based
        eventTimeThreshold = eventTimeThreshold,
        sizeBased = eventSizeThreshold > 0, //if greater than 0 then size based
        eventSizeThreshold = eventSizeThreshold,
        iGroupEventListener =
            NetworkCallManager(
                apiUrl = apiUrl,
                apiHeaders = apiHeaders
            )
        ,
        iCacheScheme = cacheScheme
    )

    init {
        (context.applicationContext as? Application)
            ?.registerActivityLifecycleCallbacks(ActivityLifecycleCallback())
    }

    companion object {
        fun init(
            context: Context,
            apiUrl: String,
            apiHeaders: HashMap<String, String> = HashMap(),
            eventThreshold: List<EventThreshold> = listOf(EventThreshold.NumBased()),
            apiKey: String,
            cacheScheme: ICacheScheme = InMemoryCacheManager(),
            eventValidationConfig: EventValidationConfig = EventValidationConfig()
        ): EventKtTracker {

            if (apiKey.isNotEmpty()) {
                apiHeaders["x-api-key"] = apiKey
            }

            val (eventNumThreshold, eventTimeThreshold, eventSizeThreshold) = validateThresholds(
                eventThreshold
            )

            return EventKtTracker(
                context = context.applicationContext,
                apiUrl = apiUrl,
                apiHeaders = apiHeaders,
                eventNumThreshold = eventNumThreshold,
                eventTimeThreshold = eventTimeThreshold,
                eventSizeThreshold = eventSizeThreshold,
                cacheScheme = cacheScheme,
                eventValidationConfig = eventValidationConfig
            )
        }
    }

    fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        val event = Event(eventName, eventParameters)
        Utils.validateEvent(
            event = event,
            eventValidationConfig = eventValidationConfig
        )
        eventManager.add(event)
    }

    fun trackAll() {
        eventManager.flushAll()
    }

}