package com.khush.sample

import android.app.Application
import android.util.Log
import com.khush.eventkt.EventKtTracker
import com.khush.eventkt.base.EventTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainApplication : Application() {

    lateinit var eventTracker: EventTracker

    override fun onCreate() {
        super.onCreate()
        //Init and add Trackers...
        eventTracker =
            EventTracker.Builder()
                .addTracker(
                    EventKtTracker.initWithCallback(
                        context = this,
                        directoryName = "events"
                    ) { jsonBody, _ ->
                        //this executes on background and need to send boolean if network call succeeds
                        //every time a group is ready, make network call
                        withContext(Dispatchers.IO) {
                            Log.i("Testing", "Callback to client with body $jsonBody")
                            delay(100)
                        }
                        return@initWithCallback true
                    }
                )
//                .addTracker(
//                    EventKtTracker.init(
//                        context = this,
//                        apiKey = "123456789",
//                        apiUrl = "https://fake-url.com"
//                    )
//                )
                .build()


        eventTracker.addBaseParams(
            hashMapOf(
                Pair("BaseKey1", "BaseValue1"),
                Pair("BaseKey2", "BaseValue2")
            )
        )
        eventTracker.addBaseParam("time", System.currentTimeMillis())
        eventTracker.track("appOpen")
    }
}