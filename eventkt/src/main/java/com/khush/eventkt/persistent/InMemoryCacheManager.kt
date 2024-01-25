package com.khush.eventkt.persistent

import androidx.annotation.CallSuper
import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventStatus
import java.util.Collections

/**
 * In memory cache manager
 *
 * Events are stored in [eventHashMap] in memory only
 *
 * Open class that can be extended and add the logic for storing events in disk
 *
 * Keep events in memory and disk synced all the time, in case of crash, events will not be lost
 *
 * [add], [addAll], [remove], [removeAll], [updateEventStatus], [updateEventStatusAll] are annotated with [CallSuper],
 * Any class extending [InMemoryCacheManager] for disk caching logic, need to write events in memory as well
 */
open class InMemoryCacheManager : ICacheScheme {

    protected val eventHashMap: MutableMap<String, Event> =
        Collections.synchronizedMap(HashMap()) //Use synchronization for iterators (like eventHashMap.values)

    @CallSuper
    override fun add(event: Event): Boolean {
        eventHashMap[event.id] = event
        return true
    }

    @CallSuper
    override fun addAll(eventList: List<Event>): Boolean {
        eventList.forEach {
            eventHashMap[it.id] = it
        }
        return true
    }

    @CallSuper
    override fun remove(event: Event): Boolean {
        eventHashMap.remove(event.id)
        return true
    }

    @CallSuper
    override fun removeAll(status: List<EventStatus>): Boolean {
        synchronized(eventHashMap) {
            eventHashMap.values.removeAll { event ->
                status.forEach { eventStatus ->
                    if (event.status == eventStatus.name) {
                        return@removeAll true
                    }
                }
                return@removeAll false
            }
        }
        return true
    }

    override fun getEvents(status: List<EventStatus>): List<Event> {
        synchronized(eventHashMap) {
            return eventHashMap.values.toList().filter { event ->
                status.forEach { eventStatus ->
                    if (event.status == eventStatus.name) {
                        return@filter true
                    }
                }
                return@filter false
            }
        }
    }

    override fun getEventSize(status: List<EventStatus>): Int {
        return getEvents(status).size
    }

    override fun getEventSizeInBytes(status: List<EventStatus>): Int {
        val eventString = getEvents(status).toString()
        val byteArray = eventString.toByteArray(Charsets.UTF_8)
        return byteArray.size
    }

    override fun syncDataToCache(success: (Boolean) -> Unit) {
        //Implement by child class (Load from memory)
    }

    override fun syncDataFromCache(success: (Boolean, List<Event>) -> Unit) {
        //Implement by child class (Load into memory)
    }

    @CallSuper
    override fun updateEventStatus(event: Event, status: EventStatus): Boolean {
        eventHashMap[event.id]?.status = status.name
        return true
    }

    @CallSuper
    override fun updateEventStatusAll(eventList: List<Event>, status: EventStatus): Boolean {
        eventList.forEach {
            eventHashMap[it.id]?.status = status.name
        }
        return true
    }

}