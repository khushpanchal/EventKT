package com.khush.eventkt.base.utils

import java.util.Collections

/**
 * Contains a list of base parameters that add up with each event parameter before getting sent to any tracker
 *
 */
internal class BaseParamUtil {

    private val baseParams: MutableMap<String, Any> =
        Collections.synchronizedMap(HashMap()) //Use synchronization for iterators (like baseParams.values)

    /**
     * Add base params
     *
     * @param params List of base parameters
     */
    fun addBaseParams(params: HashMap<String, Any>) {
        baseParams.putAll(params)
    }

    /**
     * Remove base params
     *
     * @param params List of base parameters
     */
    fun removeBaseParams(params: HashMap<String, Any>) {
        params.forEach {
            baseParams.remove(it.key)
        }
    }

    /**
     * Add base param
     *
     * @param key Single Base parameter key
     * @param value Single Base parameter value
     */
    fun addBaseParam(key: String, value: Any) {
        baseParams[key] = value
    }

    /**
     * Remove base param
     *
     * @param key Single Base parameter key
     */
    fun removeBaseParam(key: String) {
        baseParams.remove(key)
    }

    /**
     * Get base params
     *
     */
    fun getBaseParams() = baseParams
}