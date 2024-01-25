package com.khush.eventkt.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.khush.eventkt.base.ITracker


/**
 * Firebase tracker: Send events to firebase analytics library
 *
 * Implements [ITracker]
 *
 * - To initialize:-
 *
 * Add Firebase to your project [Add firebase project](https://firebase.google.com/docs/android/setup)
 * ```
 * val firebaseTracker = FirebaseTracker.init(this)
 * val eventTracker = EventTracker.Builder().addTracker(firebaseTracker).build()
 * ```
 * @param context Application context
 */
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

    /**
     * Track single event and add parameters after checking value type supported by firebase
     *
     * [Check firebase documentations for more info](https://firebase.google.com/docs/analytics/get-started?platform=android)
     *
     * @param eventName Name of the single event
     * @param eventParameters Parameters associated with the single event
     */
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

    /**
     * Set user property [Check firebase documentations for more info](https://firebase.google.com/docs/analytics/get-started?platform=android)
     *
     * @param name User specific property name
     * @param value User specific property value
     */
    fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * Set user id [Check firebase documentations for more info](https://firebase.google.com/docs/analytics/get-started?platform=android)
     *
     * @param userId User Id to uniquely identify user
     */
    fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    /**
     * Set analytics collection enabled [Check firebase documentations for more info](https://firebase.google.com/docs/analytics/get-started?platform=android)
     *
     * @param value Boolean to enable analytics for firebase
     */
    fun setAnalyticsCollectionEnabled(value: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(value)
    }

}