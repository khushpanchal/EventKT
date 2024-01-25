package com.khush.eventkt.observer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.khush.eventkt.event.EventManager


internal class AppLifecycleObserver(private val eventManager: EventManager) :
    DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        //App comes to background
        eventManager.flushAll()
    }

}