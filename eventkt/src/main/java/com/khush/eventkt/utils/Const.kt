package com.khush.eventkt.utils

import com.khush.eventkt.event.EventStatus

internal object Const {
    val allStatusList = listOf(
        EventStatus.DEFAULT,
        EventStatus.PENDING,
        EventStatus.FAILED,
        EventStatus.SUCCESS
    )

    const val EVENTS = "events"
    const val EVENT = "event"
    const val ID = "id"
    const val NAME = "name"
    const val STATUS = "status"
    const val PARAMETERS = "parameters"

    const val DEFAULT_EVENT_NUM_THRESHOLD = 10
    const val DEFAULT_EVENT_TIME_THRESHOLD = 10000L //millisecond (10seconds)
    const val DEFAULT_EVENT_SIZE_THRESHOLD = 2048 //bytes (2kb)

    const val MIN_EVENT_NUM_THRESHOLD = 1
    const val MIN_EVENT_TIME_THRESHOLD = 1L //millisecond
    const val MIN_EVENT_SIZE_THRESHOLD = 1 //byte

    const val MAX_NAME_LENGTH = 120
    const val MAX_KEY_LENGTH = 120
    const val MAX_VALUE_LENGTH = 512
    const val MAX_PARAMETERS = 256

    const val WRONG_BATCH_THRESHOLDS =
        "value passed in threshold should be greater than 0"
}