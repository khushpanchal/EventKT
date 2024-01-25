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
}