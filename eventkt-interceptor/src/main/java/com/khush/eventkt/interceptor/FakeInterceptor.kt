package com.khush.eventkt.interceptor

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException

//Added only for development purpose, this module doesn't get shipped with release
class FakeInterceptor(
    private val latency: Long = 100,
    private val responseCode: Int = 200,
    private val fail: Boolean = false,
    private val failMessage: String = "Something went wrong",
    private val headers: HashMap<String, String> = hashMapOf(
        Pair(
            "Content-Type",
            "application/json"
        )
    ),
    private val message: String = "Fake Response",
    private val contentType: String = "application/json",
    private val jsonObject: JSONObject = JSONObject().accumulate(
        "message",
        "This is a fake response"
    )
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        //simulate latency
        Thread.sleep(latency)
        // fake response
        var headerBuilder = Headers.Builder()
        headers.forEach {
            headerBuilder = headerBuilder.add(it.key, it.value)
        }
        val header = headerBuilder.build()

        //extracting request body
        val buf = okio.Buffer()
        chain.request().body?.writeTo(buf)
        val requestBodyString = buf.readUtf8()

        val fakeResponse = Response.Builder()
            .code(responseCode)
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .headers(header)
            .message(message)
            .body(
                //jsonObject.toString()
                //sending request body into response
                requestBodyString
                    .toResponseBody(contentType.toMediaTypeOrNull())
            )
            .build()

        if (fail) {
            throw IOException(failMessage)
        }
        return fakeResponse
    }
}