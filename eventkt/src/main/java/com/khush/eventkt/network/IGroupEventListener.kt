package com.khush.eventkt.network

import com.khush.eventkt.event.Event


internal interface IGroupEventListener {

    fun onEventGrouped(
        events: List<Event>,
        jsonString: String,
        networkSuccess: (Boolean, String, List<Event>) -> Unit
    )
}