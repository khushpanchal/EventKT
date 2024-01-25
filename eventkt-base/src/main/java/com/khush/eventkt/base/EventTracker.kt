package com.khush.eventkt.base

import com.khush.eventkt.base.utils.BaseParamUtil
import com.khush.eventkt.base.utils.Const

/**
 * Core class that interacts with the client
 *
 * Contains a list of all the trackers
 *
 * - To initialize and add trackers:-
 *
 * Initialize respective Trackers and build inside application onCreate()
 * ```
 * val eventTracker =
 *  EventTracker.Builder()
 *   .addTracker(EventKtTracker.init(...)) //Need to add dependency for eventkt
 *   .addTracker(FirebaseTracker.init(this)) //Need to add dependency for eventkt-firebase
 *   .addTracker(MixpanelTracker.init(this, "Add your mixpanel token")) //Need to add dependency for eventkt-mixpanel
 *   .addTracker(AmplitudeTracker.init(this, "Add your amplitude API key")) //Need to add dependency for eventkt-amplitude
 *   .build()
 * ```
 *
 * - To add base parameters:-
 * ```
 * eventTracker.addBaseParams(
 *  hashMapOf(
 *   Pair("BaseKey1", "BaseValue1"),
 *   Pair("BaseKey2", "BaseValue2")
 *  )
 * )
 * eventTracker.addBaseParam("time", System.currentTimeMillis())
 * ```
 *
 * - To use:-
 * ```
 * val parameters = hashMapOf<String, Any>()
 * parameters["eventSpecificKey1"] = "eventSpecificValue1"
 * parameters["eventSpecificKey2"] = "eventSpecificValue2"
 *
 * eventTracker.track("appOpen", parameters)
 * ```
 *
 * - To track some events for specific Tracker:-
 *
 * Say to track event "XYZ" for EventKtTracker only
 *
 * Initialize and pass the EventKtTracker to EventTracker.Builder from outside
 *
 * Use same instance of EventKtTracker passed in EventTracker.Builder and track
 *
 * ```
 * val eventKtTracker = EventKtTracker.init(...)
 * val eventTracker =
 *  EventTracker.Builder()
 *   .addTracker(eventKtTracker)
 *   .//add other trackers and build
 *
 * eventKtTracker.track("XYZ",
 *  hashMapOf<String, Any>().apply {
 *   putAll(eventTracker.getBaseParams()) //add base parameters to event specific parameters before track
 *  }
 * )
 * ```
 *
 * @property trackerList List of all the trackers that are added by the client.
 * @throws IllegalStateException If initialize without any tracker in the list
 */
class EventTracker private constructor(
    private val trackerList: List<ITracker>
) {

    init {
        if (trackerList.isEmpty()) {
            throw IllegalStateException(Const.NO_TRACKER_ADDED)
        }
    }

    private val baseParamUtil = BaseParamUtil()

    /**
     * Builder class to build the instance of [EventTracker]
     *
     */
    class Builder {
        private val trackerList = mutableListOf<ITracker>()

        /**
         * Add tracker
         *
         * @param tracker Tracker that implements [ITracker]
         * @return [Builder]
         */
        fun addTracker(tracker: ITracker): Builder {
            trackerList.add(tracker)
            return this
        }

        /**
         * Add tracker
         *
         * @param tracker Lambda function which returns Tracker that implements [ITracker]
         * @return [Builder]
         */
        fun addTracker(tracker: () -> ITracker): Builder {
            trackerList.add(tracker.invoke())
            return this
        }

        /**
         * Build instance of [EventTracker] and pass [trackerList]
         *
         */
        fun build() = EventTracker(trackerList)
    }

    /**
     * Add base parameters to each event and delegate call to each tracker in the [trackerList].
     *
     * @param eventName Name of the event
     * @param eventParameters Parameter associated with the event
     */
    @Synchronized
    fun track(eventName: String, eventParameters: HashMap<String, Any> = hashMapOf()) {
        eventParameters.putAll(baseParamUtil.getBaseParams())
        trackerList.forEach {
            it.track(eventName, eventParameters)
        }
    }

    /**
     * Immediately Track all the events
     *
     */
    @Synchronized
    fun trackAll() {
        trackerList.forEach {
            it.trackAll()
        }
    }

    /**
     * Add base params
     *
     * @param params List of base parameters
     */
    @Synchronized
    fun addBaseParams(params: HashMap<String, Any>) {
        baseParamUtil.addBaseParams(params)
    }

    /**
     * Remove base params
     *
     * @param params List of base parameters
     */
    @Synchronized
    fun removeBaseParams(params: HashMap<String, Any>) {
        baseParamUtil.removeBaseParams(params)
    }

    /**
     * Add base param
     *
     * @param key Single base parameter key
     * @param value Single base parameter value
     */
    @Synchronized
    fun addBaseParam(key: String, value: Any) {
        baseParamUtil.addBaseParam(key, value)
    }

    /**
     * Remove base param
     *
     * @param key Single base parameter key
     */
    @Synchronized
    fun removeBaseParam(key: String) {
        baseParamUtil.removeBaseParam(key)
    }

    /**
     * Get base params
     *
     */
    @Synchronized
    fun getBaseParams() = baseParamUtil.getBaseParams()

}