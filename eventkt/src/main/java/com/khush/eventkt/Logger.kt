package com.khush.eventkt


interface Logger {
    companion object {
        const val TAG = "EventKtLogs"
    }


    fun log(
        tag: String? = TAG,
        msg: String? = "",
        tr: Throwable? = null,
        type: LogType = LogType.DEBUG
    )
}


enum class LogType {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

