package me.mauricee.pontoon.domain.floatplane

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.di.AppScope
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.util.*
import javax.inject.Inject

@AppScope
class AuthInterceptor @Inject constructor(private val context: Context, private val sharedPreferences: SharedPreferences) : Interceptor {
    private val sessionRelay: Relay<Boolean> = PublishRelay.create()
    val sessionExpired: Observable<Boolean>
        get() = sessionRelay.hide()

    private val cfduid = sharedPreferences.getString(Key, UUID.randomUUID().toString()).also {
        sharedPreferences.edit { putString(Key, it) }
    }

    private var sid: String = sharedPreferences.getString(SailsSid, "")
        set(value) {
            field = value
            sharedPreferences.edit { putString(SailsSid, value) }
        }

    override fun intercept(chain: Interceptor.Chain): Response = chain.request().newBuilder()
            .addHeader("Cookie", "$CfDuid=$cfduid")
            .also { if (sid.isNotEmpty()) it.addHeader("Cookie", "$SailsSid=$sid") }
            .build().let(chain::proceed).also(::pullHeaderFromResponse).also(::checkIf403)

    private fun pullHeaderFromResponse(response: Response) {
        //KOTLIN
        response.header("set-cookie")?.split("; ")
                ?.first { it.contains(SailsSid) }?.split("$SailsSid=")
                ?.let { it[1] }?.also { sid = it }
    }

    private fun checkIf403(response: Response) {
        if (!response.isSuccessful && response.code() == HTTP_FORBIDDEN) {
            sessionRelay.accept(true)
        }
    }

    companion object {
        private const val Key = "cfuid"
        private const val SailsSid = "sails.sid"
        private const val CfDuid = "__cfduid"

    }
}