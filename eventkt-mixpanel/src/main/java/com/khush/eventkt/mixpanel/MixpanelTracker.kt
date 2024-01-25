package com.khush.eventkt.mixpanel

import android.content.Context
import com.khush.eventkt.base.ITracker
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject


class MixpanelTracker private constructor(
    context: Context,
    token: String,
    trackAutomaticEvents: Boolean
) : ITracker {

    private val mixpanelAnalytics =
        MixpanelAPI.getInstance(
            context,
            token,
            trackAutomaticEvents
        )

    companion object {
        fun init(
            context: Context,
            token: String,
            trackAutomaticEvents: Boolean = true
        ): MixpanelTracker {
            return MixpanelTracker(
                context = context.applicationContext,
                token = token,
                trackAutomaticEvents = trackAutomaticEvents
            )
        }
    }


    override fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        val props = JSONObject()
        eventParameters.forEach {
            props.put(it.key, it.value)
        }
        mixpanelAnalytics.track(eventName, props)
    }

    override fun trackAll() {
        mixpanelAnalytics.flush()
    }


    fun timeEvent(eventName: String) {
        mixpanelAnalytics.timeEvent(eventName)
    }


    fun identify(distinctId: String, usePeople: Boolean = true) {
        mixpanelAnalytics.identify(distinctId, usePeople)
    }


    fun clearTimedEvents() {
        mixpanelAnalytics.clearTimedEvents()
    }


    fun clearTimedEvent(eventName: String) {
        mixpanelAnalytics.clearTimedEvent(eventName)
    }


    fun optInTracking(distinctId: String? = null, properties: JSONObject? = null) {
        mixpanelAnalytics.optInTracking(distinctId, properties)
    }


    fun optOutTracking() {
        mixpanelAnalytics.optOutTracking()
    }


    fun reset() {
        mixpanelAnalytics.reset()
    }
}
