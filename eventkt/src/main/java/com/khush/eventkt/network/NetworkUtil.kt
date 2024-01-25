package com.khush.eventkt.network

import com.khush.eventkt.interceptor.FakeInterceptor
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

/**
 * Network util
 *
 */
internal object NetworkUtil {
    private val okhttpClient =
        OkHttpClient.Builder()
            .addInterceptor(FakeInterceptor())
            .build()

    /**
     * Perform network operation
     *
     * @param url API Url passed by client
     * @param headers API headers passed by client
     * @param body API request body for POST api call
     * @param isSuccess Lambda to invoke on success or failure of network operation
     */
    fun performNetworkOperation(
        url: String,
        headers: HashMap<String, String>,
        body: String,
        isSuccess: (Boolean) -> Unit
    ) {
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .post(body.toRequestBody())
            .build()

        okhttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isSuccess.invoke(false)
            }

            override fun onResponse(call: Call, response: Response) {
                isSuccess.invoke(true)
            }
        })
    }
}