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

/**
 * EventKtTracker: Core class that interacts with client
 *
 * Implements [ITracker]
 *
 * Two ways to initialize [EventKtTracker]:
 *
 * - To make network call at library whenever a batch of events is ready
 * ```
 * val eventKtTracker = EventKtTracker.init(
 *  context = this,
 *  apiUrl = "Your api url",
 *  apiKey = "Your api key"
 * )
 * val eventTracker = EventTracker.Builder().addTracker(eventKtTracker).build()
 *
 * //Library will add the "x-api-key" = "apiKey sent by client" into the API headers
 * //API request format sent by library
 * {
 *  “events”: [
 *   {
 *    “event”: “event 1”,
 *    “parameters”: {
 *     “param1”: “value1”,
 *     “param2”: “value2”
 *    }
 *   },
 *   …
 *   …
 *  ]
 * }
 * ```
 * - To send the list of [EventNameParam] and request body to client whenever a batch of events is ready
 * ```
 * val eventKtTracker = EventKtTracker.initWithCallback(
 *  context = this,
 *  directoryName = "Unique name of the directory to store events in disk"
 * ) { jsonBody, eventList ->
 *    // this is a suspend block runs on background thread where client can make the network call
 *    // client need to return the Boolean indicating whether network request is success or false
 *    return@initWithCallback true
 * }
 * val eventTracker = EventTracker.Builder().addTracker(eventKtTracker).build()
 * ```
 * @property eventValidationConfig Includes valid limits for [Event] properties, Also check [EventValidationConfig]
 *
 * @param context Application context
 * @param needApiCall Boolean to decide if library makes api call
 * @param apiUrl API url
 * @param apiHeaders API headers
 * @param eventNumThreshold Threshold value for count of events
 * @param eventTimeThreshold Threshold interval time in milliseconds
 * @param eventSizeThreshold Threshold value for size of events in bytes
 * @param cacheScheme Caching Strategy for memory or disk
 * @param logger Logger interface to log event addition and network results
 * @param makeNetworkRequest Lambda (suspend function) that invokes whenever the batch of events is ready for network call,
 * client passes boolean for successful or failure network call
 */
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
        /**
         * Init - To make network call at library whenever a batch of events is ready
         *
         * @param context Application context
         * @param apiUrl API url for making network request to server
         * @param apiHeaders Headers included in API request
         * @param eventThreshold List of [EventThreshold] based on which events get batched
         * @param apiKey API key that is included in headers and also help generate unique file path for event storage
         * @param cacheScheme Caching logic for storing events on disk, Default - [FileCacheManager]
         * @param eventValidationConfig Configure event limits, Default - [EventValidationConfig]
         * @param enableLogs Boolean to enable logs, Default - [BuildConfig.DEBUG]
         * @param logger Logger interface, client can implements its own Logger, Default - [EventKtLog]
         * @return Instance of [EventKtTracker]
         */
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

        /**
         * Init with callback - To send the list of [EventNameParam] and request body to client whenever a batch of events is ready
         *
         * @param context Application context
         * @param eventThreshold List of [EventThreshold] based on which events get batched
         * @param directoryName Unique directory name to store events in file system
         * @param cacheScheme Caching logic for storing events on disk, Default - [FileCacheManager]
         * @param eventValidationConfig Configure event limits, Default - [EventValidationConfig]
         * @param enableLogs Boolean to enable logs, Default - [BuildConfig.DEBUG]
         * @param logger Logger interface, client can implements its own Logger, Default - [EventKtLog]
         * @param makeNetworkRequest Lambda (suspend function) that invokes whenever the batch of events is ready for network call,
         * client passes boolean for successful or failure network call
         * @return Instance of [EventKtTracker]
         */
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

    /**
     * Validate the event as per [eventValidationConfig] and track single event, Also check [Utils.validateEvent]
     *
     * @param eventName Name of the single event
     * @param eventParameters Parameters associated with the single event
     */
    override fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        val event = Event(eventName, eventParameters)
        Utils.validateEvent(
            event = event,
            eventValidationConfig = eventValidationConfig
        )
        eventManager.add(event)
    }

    /**
     * Track all the untracked events immediately, Check [EventManager.flushAll]
     *
     */
    override fun trackAll() {
        eventManager.flushAll()
    }

}