package com.khush.sample

import android.app.Application
import com.khush.eventkt.EventKtTracker

class MainApplication : Application() {

    lateinit var eventKtTracker: EventKtTracker
    override fun onCreate() {
        super.onCreate()
        eventKtTracker = EventKtTracker.init(
            this,
            apiUrl = "https://fake-url.com",
            apiKey = "your api key"
        )
    }
}