package com.khush.eventkt

import android.app.Application
import android.content.Context
import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventManager
import com.khush.eventkt.network.NetworkCallManager
import com.khush.eventkt.observer.ActivityLifecycleCallback
import com.khush.eventkt.persistent.ICacheScheme
import com.khush.eventkt.persistent.InMemoryCacheManager
import com.khush.eventkt.utils.Const

class EventKtTracker private constructor(
    context: Context,
    apiUrl: String,
    apiHeaders: HashMap<String, String>,
    eventNumThreshold: Int,
    cacheScheme: ICacheScheme
) {

    private val eventManager = EventManager(
        eventNumThreshold = eventNumThreshold,
        iGroupEventListener =
            NetworkCallManager(
                apiUrl = apiUrl,
                apiHeaders = apiHeaders
            ),
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
            eventNumThreshold: Int = Const.DEFAULT_EVENT_NUM_THRESHOLD,
            apiKey: String,
            cacheScheme: ICacheScheme = InMemoryCacheManager()
        ): EventKtTracker {

            if (apiKey.isNotEmpty()) {
                apiHeaders["x-api-key"] = apiKey
            }

            return EventKtTracker(
                context = context.applicationContext,
                apiUrl = apiUrl,
                apiHeaders = apiHeaders,
                eventNumThreshold = eventNumThreshold,
                cacheScheme = cacheScheme
            )
        }
    }

    fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        val event = Event(eventName, eventParameters)
        eventManager.add(event)
    }

    fun trackAll() {
        eventManager.flushAll()
    }

}