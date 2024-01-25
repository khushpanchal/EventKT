package com.khush.eventkt.mixpanel

import android.content.Context
import com.khush.eventkt.base.ITracker
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

/**
 * Mixpanel tracker: Send events to mixpanel analytics library
 *
 * Implements [ITracker]
 *
 * Get unique token from Mixpanel [Get Token from Mixpanel](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
 * ```
 * val mixpanelTracker = MixpanelTracker.init(this, "your unique token")
 * val eventTracker = EventTracker.Builder().addTracker(mixpanelTracker).build()
 * ```
 * [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
 * @param context Application context
 * @param token Unique token to your project
 * @param trackAutomaticEvents Boolean to track common mobile events
 */
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

    /**
     * Track single event and add parameters as per format supported by mixpanel
     *
     * [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     * @param eventName Name of the single event
     * @param eventParameters Parameters associated with the single event
     */
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

    /**
     * Time event [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     * @param eventName The name of the event to track with timing
     */
    fun timeEvent(eventName: String) {
        mixpanelAnalytics.timeEvent(eventName)
    }

    /**
     * Identify [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     * @param distinctId String uniquely identifying the user
     * @param usePeople Boolean indicating whether or not to also call identify(String)
     */
    fun identify(distinctId: String, usePeople: Boolean = true) {
        mixpanelAnalytics.identify(distinctId, usePeople)
    }

    /**
     * Clear timed events [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     */
    fun clearTimedEvents() {
        mixpanelAnalytics.clearTimedEvents()
    }

    /**
     * Clear timed event [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     * @param eventName Name of the timed event to clear
     */
    fun clearTimedEvent(eventName: String) {
        mixpanelAnalytics.clearTimedEvent(eventName)
    }

    /**
     * Opt in tracking [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     * @param distinctId String to use as the distinct ID for events
     * @param properties JSONObject that could be passed to add properties to the opt-in event
     */
    fun optInTracking(distinctId: String? = null, properties: JSONObject? = null) {
        mixpanelAnalytics.optInTracking(distinctId, properties)
    }

    /**
     * Opt out tracking [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     */
    fun optOutTracking() {
        mixpanelAnalytics.optOutTracking()
    }

    /**
     * Reset [Check mixpanel documentations for more info](https://docs.mixpanel.com/docs/tracking-methods/sdks/android)
     *
     */
    fun reset() {
        mixpanelAnalytics.reset()
    }
}
