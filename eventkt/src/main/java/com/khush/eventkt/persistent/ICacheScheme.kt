package com.khush.eventkt.persistent

import com.khush.eventkt.event.Event
import com.khush.eventkt.event.EventStatus
import com.khush.eventkt.utils.Const

/**
 * Interface with common persistent methods
 *
 * Client can implement [ICacheScheme] and pass it to library to have it's own implementation
 *
 * However it is suggested to extend [InMemoryCacheManager] instead to have your own implementation
 *
 * And keep the caching logic in a way it is synced with memory all the time
 */
interface ICacheScheme {
    /**
     * Add Single event to the memory
     *
     * @param event Single [Event]
     * @return Boolean for successful addition
     */
    fun add(event: Event): Boolean

    /**
     * Add List of events to the memory
     *
     * @param eventList List of [Event]
     * @return Boolean for successful addition
     */
    fun addAll(eventList: List<Event>): Boolean

    /**
     * Remove single event from the memory
     *
     * @param event Single [Event]
     * @return Boolean for successful removal
     */
    fun remove(event: Event): Boolean

    /**
     * Remove list of events from the memory based on the status
     *
     * @param status List of [EventStatus] for which events get removed
     * @return Boolean for successful removal
     */
    fun removeAll(status: List<EventStatus> = Const.allStatusList): Boolean

    /**
     * Get List of events from the memory based on the status
     *
     * @param status List of [EventStatus] for which events get fetched
     * @return List of [Event] with passed status list
     */
    fun getEvents(status: List<EventStatus> = Const.allStatusList): List<Event>

    /**
     * Get count of events in memory based on the status
     *
     * @param status List of [EventStatus] for which count get fetched
     * @return Total count of events in memory
     */
    fun getEventSize(status: List<EventStatus> = Const.allStatusList): Int

    /**
     * Get size of events in memory in bytes based on the status
     *
     * @param status List of [EventStatus] for which size get fetched
     * @return Total size of events in bytes
     */
    fun getEventSizeInBytes(status: List<EventStatus> = Const.allStatusList): Int

    /**
     * Fetch in memory events and push all the events to disk
     *
     * @param success Lambda(Boolean) to invoke after successful write to disk
     */
    fun syncDataToCache(success: (Boolean) -> Unit = {})

    /**
     * Fetch events from the disk
     *
     * @param success Lambda(Boolean, List of [Event]) to invoke after successful fetch from disk
     */
    fun syncDataFromCache(success: (Boolean, List<Event>) -> Unit = { _, _ -> })

    /**
     * Update status for the event to the memory
     *
     * @param event Single [Event]
     * @param status [EventStatus] to update in event
     * @return Boolean for successful update
     */
    fun updateEventStatus(event: Event, status: EventStatus): Boolean

    /**
     * Update status for the list of events to the memory
     *
     * @param eventList List of [Event]
     * @param status [EventStatus] to update in the list of event
     * @return Boolean for successful update
     */
    fun updateEventStatusAll(eventList: List<Event>, status: EventStatus): Boolean
}
