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
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


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

    fun List<Event>.toJson(): String {
        val jsonArray = JSONArray()
        this.forEach {
            val jsonBody = JSONObject()
            jsonBody.put(NAME, it.name)
            val parameterJson = JSONObject()
            it.parameters.forEach { param ->
                parameterJson.put(param.key, param.value)
            }
            jsonBody.put(PARAMETERS, parameterJson)
            jsonBody.put(ID, it.id)
            jsonBody.put(STATUS, it.status)
            jsonArray.put(jsonBody)
        }
        return jsonArray.toString()
    }

    fun fromJsonStringToEventList(jsonString: String): List<Event> {
        val eventList = mutableListOf<Event>()
        val jsonArray = JSONArray(jsonString)

        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val event = Event(
                name = jsonObject.getString(NAME),
                id = jsonObject.getString(ID),
                status = jsonObject.getString(STATUS)
            )
            val paramJsonObject = jsonObject.getJSONObject(PARAMETERS)
            val paramKeys = paramJsonObject.keys()
            while (paramKeys.hasNext()) {
                val keyValue = paramKeys.next()
                val paramValue: String = paramJsonObject.getString(keyValue)
                event.parameters[keyValue] = paramValue
            }
            eventList.add(event)
        }
        return eventList
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

    fun generateFilePath(string: String): String {
        val hash: ByteArray = try {
            MessageDigest.getInstance("MD5").digest(string.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("NoSuchAlgorithmException", e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("UnsupportedEncodingException", e)
        }

        val hex = StringBuilder(hash.size * 2)

        for (b in hash) {
            if (b and 0xFF.toByte() < 0x10) hex.append("0")
            hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
        }

        return hex.toString().hashCode().toString()
    }

}