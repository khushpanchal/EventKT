package com.khush.eventkt

import com.khush.eventkt.utils.Const

/**
 * Contains thresholds after which all untracked events will be sent to network
 *
 */
sealed interface EventThreshold {
    /**
     * Event count based threshold
     *
     * @property value Threshold number of events after which untracked events will be sent to network,
     * Default - [Const.DEFAULT_EVENT_NUM_THRESHOLD]
     */
    data class NumBased(val value: Int = Const.DEFAULT_EVENT_NUM_THRESHOLD) : EventThreshold

    /**
     * Time based threshold
     *
     * @property value Threshold time in milliseconds after which untracked events will be sent to network,
     * Default - [Const.DEFAULT_EVENT_TIME_THRESHOLD]
     */
    data class TimeBased(val value: Long = Const.DEFAULT_EVENT_TIME_THRESHOLD) : EventThreshold

    /**
     * Size based threshold
     *
     * @property value Threshold size in bytes after which untracked events will be sent to network,
     * Default - [Const.DEFAULT_EVENT_SIZE_THRESHOLD]
     */
    data class SizeBased(val value: Int = Const.DEFAULT_EVENT_SIZE_THRESHOLD) : EventThreshold
}