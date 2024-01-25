package com.khush.eventkt

import com.khush.eventkt.utils.Const

sealed interface EventThreshold {

    data class NumBased(val value: Int = Const.DEFAULT_EVENT_NUM_THRESHOLD) : EventThreshold

    data class TimeBased(val value: Long = Const.DEFAULT_EVENT_TIME_THRESHOLD) : EventThreshold

    data class SizeBased(val value: Int = Const.DEFAULT_EVENT_SIZE_THRESHOLD) : EventThreshold
}