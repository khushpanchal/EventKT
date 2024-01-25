package com.khush.eventkt.persistent

import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventStatus
import com.khush.eventkt.utils.Const


interface ICacheScheme {

    fun add(event: Event): Boolean


    fun addAll(eventList: List<Event>): Boolean


    fun remove(event: Event): Boolean


    fun removeAll(status: List<EventStatus> = Const.allStatusList): Boolean


    fun getEvents(status: List<EventStatus> = Const.allStatusList): List<Event>


    fun getEventSize(status: List<EventStatus> = Const.allStatusList): Int


    fun getEventSizeInBytes(status: List<EventStatus> = Const.allStatusList): Int


    fun syncDataToCache(success: (Boolean) -> Unit = {})


    fun syncDataFromCache(success: (Boolean, List<Event>) -> Unit = { _, _ -> })


    fun updateEventStatus(event: Event, status: EventStatus): Boolean


    fun updateEventStatusAll(eventList: List<Event>, status: EventStatus): Boolean
}
