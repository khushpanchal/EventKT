package com.khush.sample

import android.app.Application
import android.util.Log
import com.khush.eventkt.EventKtTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainApplication : Application() {

    lateinit var eventKtTracker: EventKtTracker
    override fun onCreate() {
        super.onCreate()
        eventKtTracker =
//            EventKtTracker.init(
//            this,
//            apiUrl = "https://fake-url.com",
//            apiKey = "your api key"
//        )
            EventKtTracker.initWithCallback(
                context = this,
                directoryName = "events"
            ) { jsonBody, _ ->
                //this executes on background and need to send boolean if network call succeeds
                //every time a group is ready, make network call
                withContext(Dispatchers.IO) {
                    Log.i("EventKtLogs", "Callback to client with body $jsonBody")
                    delay(100)
                }
                return@initWithCallback true
            }
    }
}