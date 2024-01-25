package com.khush.eventkt.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.khush.eventkt.base.ITracker



class FirebaseTracker private constructor(
    context: Context
) : ITracker {

    private val firebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    companion object {
        fun init(context: Context): FirebaseTracker {
            return FirebaseTracker(context.applicationContext)
        }
    }


    override fun track(eventName: String, eventParameters: HashMap<String, Any>) {
        firebaseAnalytics.logEvent(eventName) {
            eventParameters.forEach {
                when (it.value) {
                    is String -> param(it.key, it.value as String)
                    is Long -> param(it.key, it.value as Long)
                    is Double -> param(it.key, it.value as Double)
                    is Bundle -> param(it.key, it.value as Bundle)
                    is Array<*> -> {
                        if (it.value.javaClass.componentType == Bundle::class.java) {
                            param(it.key, it.value as Array<Bundle>)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun trackAll() {}


    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }


    fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }


    fun setAnalyticsCollectionEnabled(value: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(value)
    }

}