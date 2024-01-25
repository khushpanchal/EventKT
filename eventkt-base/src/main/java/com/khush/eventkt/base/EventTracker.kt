package com.khush.eventkt.base

import com.khush.eventkt.base.utils.BaseParamUtil
import com.khush.eventkt.base.utils.Const


class EventTracker private constructor(
    private val trackerList: List<ITracker>
) {

    init {
        if (trackerList.isEmpty()) {
            throw IllegalStateException(Const.NO_TRACKER_ADDED)
        }
    }

    private val baseParamUtil = BaseParamUtil()


    class Builder {
        private val trackerList = mutableListOf<ITracker>()


        fun addTracker(tracker: ITracker): Builder {
            trackerList.add(tracker)
            return this
        }


        fun addTracker(tracker: () -> ITracker): Builder {
            trackerList.add(tracker.invoke())
            return this
        }


        fun build() = EventTracker(trackerList)
    }


    @Synchronized
    fun track(eventName: String, eventParameters: HashMap<String, Any> = hashMapOf()) {
        eventParameters.putAll(baseParamUtil.getBaseParams())
        trackerList.forEach {
            it.track(eventName, eventParameters)
        }
    }


    @Synchronized
    fun trackAll() {
        trackerList.forEach {
            it.trackAll()
        }
    }


    @Synchronized
    fun addBaseParams(params: HashMap<String, Any>) {
        baseParamUtil.addBaseParams(params)
    }


    @Synchronized
    fun removeBaseParams(params: HashMap<String, Any>) {
        baseParamUtil.removeBaseParams(params)
    }


    @Synchronized
    fun addBaseParam(key: String, value: Any) {
        baseParamUtil.addBaseParam(key, value)
    }


    @Synchronized
    fun removeBaseParam(key: String) {
        baseParamUtil.removeBaseParam(key)
    }


    @Synchronized
    fun getBaseParams() = baseParamUtil.getBaseParams()

}