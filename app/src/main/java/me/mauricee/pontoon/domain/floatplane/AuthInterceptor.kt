package me.mauricee.pontoon.domain.floatplane

import android.content.SharedPreferences
import androidx.core.content.edit
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.logd
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.util.*
import javax.inject.Inject

@AppScope
class AuthInterceptor @Inject constructor(private val accountManager: AccountManagerHelper) : Interceptor {

    private var sid
        get() = accountManager.sid
        set(value) {
            accountManager.sid = value
        }

    private val cfduid
        get() = accountManager.cfduid

    private val sessionRelay: Relay<Boolean> = PublishRelay.create()
    val sessionExpired: Observable<Boolean>
        get() = sessionRelay.hide()

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.request().newBuilder()
                .addHeader("Cookie", "$CfDuid=$cfduid")
                .also { if (sid.isNotEmpty()) it.addHeader("Cookie", "$SailsSid=$sid") }
                .build().let(chain::proceed).also(::pullHeaderFromResponse).also(::checkIf403)
    }

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
        const val SailsSid = "sails.sid"
        const val CfDuid = "__cfduid"
    }
}