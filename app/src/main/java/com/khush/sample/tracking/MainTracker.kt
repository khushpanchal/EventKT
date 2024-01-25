package com.khush.sample.tracking

import android.content.Context
import com.khush.sample.MainApplication

class MainTracker(
    private val context: Context
) {

    fun rvItemClicked(itemName: String, itemPos: Int) {
        val parameters = hashMapOf<String, Any>()
        parameters["itemName"] = itemName
        parameters["itemPos"] = itemPos
        track("rvItemClicked", parameters)
    }

    fun screenOpen(screenName: String) {
        val parameters = hashMapOf<String, Any>()
        parameters["screenName"] = screenName
        track("screenOpen", parameters)
    }

    fun goBackClicked(screenName: String) {
        val parameters = hashMapOf<String, Any>()
        parameters["screenName"] = screenName
        track("goBackClicked", parameters)
    }

    fun networkCall(name: String) {
        val parameters = hashMapOf<String, Any>()
        parameters["name"] = name
        track("networkCall", parameters)
    }

    private fun track(name: String, params: HashMap<String, Any>) {
        (context.applicationContext as MainApplication).eventKtTracker.track(name, params)
    }

}