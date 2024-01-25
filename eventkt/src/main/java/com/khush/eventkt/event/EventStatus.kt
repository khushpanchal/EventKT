package com.khush.eventkt.event

/**
 * Defines the status of the event at any point
 *
 */
enum class EventStatus {
    /**
     * Default: Initial status of event when sent to library
     *
     */
    DEFAULT, //sent to library

    /**
     * Pending: Status of event when sent to network
     *
     */
    PENDING, //sent to network

    /**
     * Success: Status of event when network call is success
     *
     */
    SUCCESS, //network success

    /**
     * Failed: Status of event when network call is failed
     *
     */
    FAILED //network failure
}