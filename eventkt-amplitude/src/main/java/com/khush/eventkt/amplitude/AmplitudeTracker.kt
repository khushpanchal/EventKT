package com.khush.eventkt.amplitude

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.amplitude.core.ServerZone
import com.khush.eventkt.base.ITracker

/**
 * Amplitude tracker: Send events to amplitude analytics library
 *
 * Implements [ITracker]
 *
 * Get unique api key from Amplitude [Get API Key from Amplitude](https://www.docs.developers.amplitude.com/analytics/find-api-credentials/)
 * ```
 * val amplitudeTracker = AmplitudeTracker.init(this, "your unique api key")
 * val eventTracker = EventTracker.Builder().addTracker(amplitudeTracker).build()
 * ```
 * [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
 * @param context Application context
 * @param apiKey Unique API key for your project
 * @param sessions Boolean to enable session events tracking
 * @param appLifecycles Boolean to enable app lifecycle events tracking
 * @param deepLinks Boolean to enable deeplink events tracking
 * @param screenViews Boolean to enable screen view events tracking
 */
class AmplitudeTracker private constructor(
    context: Context,
    apiKey: String,
    sessions: Boolean,
    appLifecycles: Boolean,
    deepLinks: Boolean,
    screenViews: Boolean
) : ITracker {

    private val amplitudeAnalytics = Amplitude(
        Configuration(
            apiKey = apiKey,
            context = context,
            defaultTracking = DefaultTrackingOptions(
                sessions = sessions,
                appLifecycles = appLifecycles,
                deepLinks = deepLinks,
                screenViews = screenViews
            )
        )
    )

    companion object {
        fun init(
            context: Context,
            apiKey: String,
            sessions: Boolean = true,
            appLifecycles: Boolean = false,
            deepLinks: Boolean = false,
            screenViews: Boolean = false
        ): AmplitudeTracker {
            return AmplitudeTracker(
                context.applicationContext,
                apiKey,
                sessions,
                appLifecycles,
                deepLinks,
                screenViews
            )
        }
    }

    override fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        amplitudeAnalytics.track(eventName, eventParameters)
    }

    override fun trackAll() {}

    /**
     * Opt out [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
     *
     * @param value Boolean to opt the user out of tracking
     */
    fun optOut(value: Boolean) {
        amplitudeAnalytics.configuration.optOut = value
    }

    /**
     * Set EU server zone [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
     *
     */
    fun setEUServerZone() {
        amplitudeAnalytics.configuration.serverZone = ServerZone.EU
    }

    /**
     * Set US server zone [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
     *
     */
    fun setUSServerZone() {
        amplitudeAnalytics.configuration.serverZone = ServerZone.US
    }

    /**
     * Set server url [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
     *
     * @param serverUrl Server Url events upload to
     */
    fun setServerUrl(serverUrl: String) {
        amplitudeAnalytics.configuration.serverUrl = serverUrl
    }

    /**
     * Set user id [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
     *
     * @param userId Unique user Id
     */
    fun setUserId(userId: String?) {
        amplitudeAnalytics.setUserId(userId)
    }

    /**
     * Set device id [Check amplitude documentations for more info](https://www.docs.developers.amplitude.com/data/sdks/android-kotlin/)
     *
     * @param deviceId Unique device Id
     */
    fun setDeviceId(deviceId: String) {
        amplitudeAnalytics.setDeviceId(deviceId)
    }

}