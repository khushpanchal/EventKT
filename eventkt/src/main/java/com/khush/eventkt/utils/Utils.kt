package com.khush.eventkt.utils

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

}