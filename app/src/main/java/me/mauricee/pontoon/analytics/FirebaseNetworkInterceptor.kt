package me.mauricee.pontoon.analytics

import com.google.firebase.perf.FirebasePerformance
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*
import javax.inject.Inject

class FirebaseNetworkInterceptor @Inject constructor(private val manager: PrivacyManager) : Interceptor {
    private val perf: FirebasePerformance by lazy { FirebasePerformance.getInstance() }
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (!manager.isAnalyticsEnabled) chain.proceed(chain.request())
        else {
            val request = chain.request()
            val metric = perf.newHttpMetric(request.url.toString(),
                    request.method.capitalize(Locale.ROOT))
            metric.setRequestPayloadSize(request.body?.contentLength() ?: 0)
            val response = chain.proceed(request)
            metric.setHttpResponseCode(response.code)
            metric.setResponsePayloadSize(response.body?.contentLength() ?: 0)
            metric.stop()
            response
        }
    }
}