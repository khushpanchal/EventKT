package com.khush.eventkt.utils

import java.util.Collections

internal class BaseParamUtil {

    private val baseParams: MutableMap<String, Any> =
        Collections.synchronizedMap(HashMap()) //Use synchronization for iterators (like baseParams.values)

    fun addBaseParams(params: HashMap<String, Any>) {
        baseParams.putAll(params)
    }

    fun removeBaseParams(params: HashMap<String, Any>) {
        params.forEach {
            baseParams.remove(it.key)
        }
    }

    fun addBaseParam(key: String, value: Any) {
        baseParams[key] = value
    }

    fun removeBaseParam(key: String) {
        baseParams.remove(key)
    }

    fun getBaseParams() = baseParams
}