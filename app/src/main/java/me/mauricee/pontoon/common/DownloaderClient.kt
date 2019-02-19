package me.mauricee.pontoon.common

import com.novoda.downloadmanager.HttpClient
import com.novoda.downloadmanager.NetworkRequest
import com.novoda.downloadmanager.NetworkResponse
import okhttp3.OkHttpClient
import javax.inject.Inject
import okhttp3.Request
import java.io.IOException
import java.io.InputStream


class DownloaderClient @Inject constructor(private val okHttpClient: OkHttpClient): HttpClient {
    override fun execute(request: NetworkRequest): NetworkResponse {
        var requestBuilder = Request.Builder()
                .url(request.url())

        if (request.method() === NetworkRequest.Method.HEAD) {
            requestBuilder = requestBuilder.head()
        }

        request.headers().entries.forEach { requestBuilder.addHeader(it.key, it.value) }


        val call = okHttpClient.newCall(requestBuilder.build())

        return Response(call.execute())
    }


    private inner class Response(private val response: okhttp3.Response) : NetworkResponse {

        override fun code(): Int {
            return response.code()
        }

        override fun isSuccessful(): Boolean {
            return response.isSuccessful
        }

        override fun header(name: String, defaultValue: String): String {
            return response.header(name, defaultValue)!!
        }

        @Throws(IOException::class)
        override fun openByteStream(): InputStream {
            return response.body()!!.byteStream()
        }

        @Throws(IOException::class)
        override fun closeByteStream() {
            response.body()?.close()
        }

        override fun bodyContentLength(): Long = response.body()?.contentLength() ?: 0
    }
}