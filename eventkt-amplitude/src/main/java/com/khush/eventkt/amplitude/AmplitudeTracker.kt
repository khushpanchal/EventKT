package com.khush.eventkt.amplitude

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.amplitude.core.ServerZone
import com.khush.eventkt.base.ITracker


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


    fun optOut(value: Boolean) {
        amplitudeAnalytics.configuration.optOut = value
    }


    fun setEUServerZone() {
        amplitudeAnalytics.configuration.serverZone = ServerZone.EU
    }


    fun setUSServerZone() {
        amplitudeAnalytics.configuration.serverZone = ServerZone.US
    }


    fun setServerUrl(serverUrl: String) {
        amplitudeAnalytics.configuration.serverUrl = serverUrl
    }


    fun setUserId(userId: String?) {
        amplitudeAnalytics.setUserId(userId)
    }


    fun setDeviceId(deviceId: String) {
        amplitudeAnalytics.setDeviceId(deviceId)
    }

}