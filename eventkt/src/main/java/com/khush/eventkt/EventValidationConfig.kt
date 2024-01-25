package com.khush.eventkt

import com.khush.eventkt.utils.Const

/**
 * Event validation config: Contains maximum limits of different parameters
 *
 * @property maxNameLength Maximum length of event name, Default - [Const.MAX_NAME_LENGTH]
 * @property maxKeyLength Maximum key length for event parameter, Default - [Const.MAX_KEY_LENGTH]
 * @property maxValueLength Maximum value length for event parameter, Default - [Const.MAX_VALUE_LENGTH]
 * @property maxParameters Maximum parameter count per event, Default - [Const.MAX_PARAMETERS]
 */
data class EventValidationConfig(
    val maxNameLength: Int = Const.MAX_NAME_LENGTH,
    val maxKeyLength: Int = Const.MAX_KEY_LENGTH,
    val maxValueLength: Int = Const.MAX_VALUE_LENGTH,
    val maxParameters: Int = Const.MAX_PARAMETERS
)
