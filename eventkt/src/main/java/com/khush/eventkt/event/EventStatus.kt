package com.khush.eventkt.event


enum class EventStatus {

    DEFAULT, //sent to library

    PENDING, //sent to network

    SUCCESS, //network success

    FAILED //network failure
}