package com.khush.eventkt.observer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.khush.eventkt.event.EventManager

/**
 * Observes App lifecycle and [EventManager.flushAll] flushes event whenever app goes to background
 *
 * @property eventManager Check [EventManager]
 */
internal class AppLifecycleObserver(private val eventManager: EventManager) :
    DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        //App comes to foreground
        eventManager.startTimer()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        //App comes to background
        eventManager.stopTimer()
        eventManager.flushAll()
    }

}