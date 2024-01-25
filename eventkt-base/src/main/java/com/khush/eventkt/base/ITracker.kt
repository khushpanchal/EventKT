package com.khush.eventkt.base

/**
 * All trackers in different modules need to implement this
 *
 */
interface ITracker {
    /**
     * Track single event
     *
     * @param eventName Name of the single event
     * @param eventParameters Parameters associated with the single event
     */
    fun track(eventName: String, eventParameters: HashMap<String, Any> = hashMapOf())

    /**
     * Track all the untracked events immediately
     *
     */
    fun trackAll()
}