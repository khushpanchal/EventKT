package com.khush.eventkt

import com.khush.eventkt.utils.Const


data class EventValidationConfig(
    val maxNameLength: Int = Const.MAX_NAME_LENGTH,
    val maxKeyLength: Int = Const.MAX_KEY_LENGTH,
    val maxValueLength: Int = Const.MAX_VALUE_LENGTH,
    val maxParameters: Int = Const.MAX_PARAMETERS
)
