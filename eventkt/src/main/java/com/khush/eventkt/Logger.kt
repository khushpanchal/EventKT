package com.khush.eventkt

/**
 * Implement the Logger interface to get the logs as per the requirement
 *
 */
interface Logger {
    companion object {
        const val TAG = "EventKtLogs"
    }

    /**
     * Log the message and delegate as per the implementation
     *
     * @param tag Used to identify the source of a log message
     * @param msg The message to be logged
     * @param tr An exception to log
     * @param type Enum class with different log levels, Default - [LogType.DEBUG]
     */
    fun log(
        tag: String? = TAG,
        msg: String? = "",
        tr: Throwable? = null,
        type: LogType = LogType.DEBUG
    )
}

/**
 * Log type: Different log levels
 *
 */
enum class LogType {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

