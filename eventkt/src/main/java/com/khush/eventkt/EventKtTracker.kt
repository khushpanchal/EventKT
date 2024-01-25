package com.khush.eventkt

import android.app.Application
import android.content.Context
import com.khush.eventkt.base.ITracker
import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventManager
import com.khush.eventkt.network.ClientCallbackProvider
import com.khush.eventkt.network.EventNameParam
import com.khush.eventkt.network.NetworkCallManager
import com.khush.eventkt.observer.ActivityLifecycleCallback
import com.khush.eventkt.persistent.FileCacheManager
import com.khush.eventkt.persistent.ICacheScheme
import com.khush.eventkt.utils.EventKtLog
import com.khush.eventkt.utils.Utils
import com.khush.eventkt.utils.Utils.generateFilePath
import com.khush.eventkt.utils.Utils.validateThresholds


class EventKtTracker private constructor(
    context: Context,
    needApiCall: Boolean,
    apiUrl: String,
    apiHeaders: HashMap<String, String>,
    eventNumThreshold: Int,
    eventTimeThreshold: Long,
    eventSizeThreshold: Int,
    cacheScheme: ICacheScheme,
    private val eventValidationConfig: EventValidationConfig,
    logger: Logger,
    makeNetworkRequest: suspend (String, List<EventNameParam>) -> Boolean
) : ITracker {

    private val eventManager = EventManager(
        numBased = eventNumThreshold > 0, //if greater than 0 then count based
        eventNumThreshold = eventNumThreshold,
        timeBased = eventTimeThreshold > 0, //if greater than 0 then time based
        eventTimeThreshold = eventTimeThreshold,
        sizeBased = eventSizeThreshold > 0, //if greater than 0 then size based
        eventSizeThreshold = eventSizeThreshold,
        iGroupEventListener = if (needApiCall) {
            NetworkCallManager(
                apiUrl = apiUrl,
                apiHeaders = apiHeaders
            )
        } else {
            ClientCallbackProvider(
                makeNetworkRequest = makeNetworkRequest
            )
        },
        iCacheScheme = cacheScheme,
        logger = logger
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
            cacheScheme: ICacheScheme = FileCacheManager(
                context = context.applicationContext,
                filePath = generateFilePath(apiKey)
            ),
            eventValidationConfig: EventValidationConfig = EventValidationConfig(),
            enableLogs: Boolean = BuildConfig.DEBUG,
            logger: Logger = EventKtLog(enableLogs)
        ): EventKtTracker {

            val (eventNumThreshold, eventTimeThreshold, eventSizeThreshold) = validateThresholds(
                eventThreshold
            )

            if (apiKey.isNotEmpty()) {
                apiHeaders["x-api-key"] = apiKey
            }

            return EventKtTracker(
                context = context.applicationContext,
                needApiCall = true,
                apiUrl = apiUrl,
                apiHeaders = apiHeaders,
                eventNumThreshold = eventNumThreshold,
                eventTimeThreshold = eventTimeThreshold,
                eventSizeThreshold = eventSizeThreshold,
                cacheScheme = cacheScheme,
                eventValidationConfig = eventValidationConfig,
                logger = logger,
                makeNetworkRequest = { _, _ -> false }
            )
        }


        fun initWithCallback(
            context: Context,
            eventThreshold: List<EventThreshold> = listOf(EventThreshold.NumBased()),
            directoryName: String,
            cacheScheme: ICacheScheme = FileCacheManager(
                context = context.applicationContext,
                filePath = directoryName
            ),
            eventValidationConfig: EventValidationConfig = EventValidationConfig(),
            enableLogs: Boolean = BuildConfig.DEBUG,
            logger: Logger = EventKtLog(enableLogs),
            makeNetworkRequest: suspend (String, List<EventNameParam>) -> Boolean
        ): EventKtTracker {
            val (eventNumThreshold, eventTimeThreshold, eventSizeThreshold) = validateThresholds(
                eventThreshold
            )

            return EventKtTracker(
                context = context.applicationContext,
                needApiCall = false,
                apiUrl = "",
                apiHeaders = hashMapOf(),
                eventNumThreshold = eventNumThreshold,
                eventTimeThreshold = eventTimeThreshold,
                eventSizeThreshold = eventSizeThreshold,
                cacheScheme = cacheScheme,
                eventValidationConfig = eventValidationConfig,
                logger = logger,
                makeNetworkRequest = makeNetworkRequest
            )
        }
    }


    override fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        val event = Event(eventName, eventParameters)
        Utils.validateEvent(
            event = event,
            eventValidationConfig = eventValidationConfig
        )
        eventManager.add(event)
    }


    override fun trackAll() {
        eventManager.flushAll()
    }

}