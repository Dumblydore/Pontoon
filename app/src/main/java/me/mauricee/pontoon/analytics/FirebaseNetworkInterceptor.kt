package me.mauricee.pontoon.analytics

import com.google.firebase.perf.FirebasePerformance
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class FirebaseNetworkInterceptor @Inject constructor(private val perf: FirebasePerformance) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val metric = perf.newHttpMetric(request.url().toString(),
                request.method().capitalize())
        metric.setRequestPayloadSize(request.body()?.contentLength() ?: 0)
        val response = chain.proceed(request)
        metric.setHttpResponseCode(response.code())
        metric.setResponsePayloadSize(response.body()?.contentLength() ?: 0)
        metric.stop()
        return response
    }
}