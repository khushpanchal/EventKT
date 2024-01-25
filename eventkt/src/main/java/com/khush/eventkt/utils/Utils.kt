package com.khush.eventkt.utils

import com.khush.eventkt.EventThreshold
import com.khush.eventkt.EventValidationConfig
import com.khush.eventkt.event.Event
import com.khush.eventkt.network.model.RequestBody
import com.khush.eventkt.utils.Const.EVENT
import com.khush.eventkt.utils.Const.EVENTS
import com.khush.eventkt.utils.Const.ID
import com.khush.eventkt.utils.Const.NAME
import com.khush.eventkt.utils.Const.PARAMETERS
import com.khush.eventkt.utils.Const.STATUS
import org.json.JSONArray
import org.json.JSONObject


internal object Utils {

    fun RequestBody.toJson(): String {
        val requestBody = JSONObject()
        val eventArray = JSONArray()
        this.events.forEach { eventsRequestBody ->
            val eventBody = JSONObject()
            eventBody.put(EVENT, eventsRequestBody.event)
            val eventParams = JSONObject()
            eventsRequestBody.parameters?.forEach {
                eventParams.put(it.key, it.value)
            }
            eventBody.put(PARAMETERS, eventParams)
            eventArray.put(eventBody)
        }
        requestBody.put(EVENTS, eventArray)
        return requestBody.toString()
    }

    fun Event.toJson(): String {
        val jsonBody = JSONObject()
        jsonBody.put(NAME, this.name)
        val parameterJson = JSONObject()
        this.parameters.forEach { param ->
            parameterJson.put(param.key, param.value)
        }
        jsonBody.put(PARAMETERS, parameterJson)
        jsonBody.put(ID, this.id)
        jsonBody.put(STATUS, this.status)
        return jsonBody.toString()
    }

    fun validateEvent(event: Event, eventValidationConfig: EventValidationConfig) {
        // Check length of name
        if (event.name.length > eventValidationConfig.maxNameLength) {
            throw IllegalArgumentException("Event Name length exceeds ${eventValidationConfig.maxNameLength} characters.")
        }

        // Check number of parameters
        if (event.parameters.size > eventValidationConfig.maxParameters) {
            throw IllegalArgumentException("Number of parameters exceeds ${eventValidationConfig.maxParameters}.")
        }

        // Check length of keys and values in the HashMap
        for ((key, value) in event.parameters) {
            // Check length of key
            if (key.length > eventValidationConfig.maxKeyLength) {
                throw IllegalArgumentException("Key '$key' length exceeds ${eventValidationConfig.maxKeyLength} characters.")
            }

            // Check length of value
            val valueString = value.toString()
            if (valueString.length > eventValidationConfig.maxValueLength) {
                throw IllegalArgumentException("Value '$valueString' length exceeds ${eventValidationConfig.maxValueLength} characters.")
            }

            // Check type of value
            if (!isPrimitiveType(value)) {
                throw IllegalArgumentException("Value '$valueString' is not of primitive type.")
            }
        }
    }

    private fun isPrimitiveType(value: Any): Boolean {
        return value is String || value is Int || value is Double || value is Float || value is Long
    }

    fun validateThresholds(eventThreshold: List<EventThreshold>): Triple<Int, Long, Int> {
        var eventNumThreshold = 0
        var eventTimeThreshold = 0L
        var eventSizeThreshold = 0

        eventThreshold.forEach {
            when (it) {
                is EventThreshold.NumBased -> eventNumThreshold = it.value
                is EventThreshold.TimeBased -> eventTimeThreshold = it.value
                is EventThreshold.SizeBased -> eventSizeThreshold = it.value
            }
        }

        if (eventNumThreshold < Const.MIN_EVENT_NUM_THRESHOLD &&
            eventTimeThreshold < Const.MIN_EVENT_TIME_THRESHOLD &&
            eventSizeThreshold < Const.MIN_EVENT_SIZE_THRESHOLD
        ) {
            throw IllegalArgumentException(Const.WRONG_BATCH_THRESHOLDS)
        }
        return Triple(eventNumThreshold, eventTimeThreshold, eventSizeThreshold)
    }

}