package com.khush.eventkt.interceptor

import okhttp3.Interceptor
import okhttp3.Response


class FakeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // no-op
        return chain.proceed(chain.request())
    }
}