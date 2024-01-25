package com.khush.eventkt.base


interface ITracker {

    fun track(eventName: String, eventParameters: HashMap<String, Any> = hashMapOf())


    fun trackAll()
}