package me.mauricee.pontoon.domain.floatplane

import androidx.datastore.core.DataStore
import androidx.datastore.rxjava2.data
import androidx.datastore.rxjava2.updateDataAsync
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.model.session.SessionCredentials
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val credentials: DataStore<SessionCredentials>) : Interceptor {

    private var sid: String = ""
    private var cfduid: String = ""
    private val sessionRelay: Relay<Unit> = PublishRelay.create()
    private val subs = CompositeDisposable()

    val sessionExpired: Observable<Unit>
        get() = sessionRelay.hide()

    init {
        subs += credentials.data().subscribe {
            sid = it.sid
            cfduid = it.cfuId
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.request().newBuilder()
                .addHeader("Cookie", "$CfDuid=$cfduid")
                .also { if (sid.isNotEmpty()) it.addHeader("Cookie", "$SailsSid=$sid") }
                .build().let(chain::proceed).also(::pullHeaderFromResponse).also(::checkIf403)
    }

    private fun pullHeaderFromResponse(response: Response) {
        try {
            response.header("set-cookie")?.split("; ")
                    ?.firstOrNull { it.contains(SailsSid) }?.split("$SailsSid=")
                    ?.let { it[1] }?.also { newSid ->
                        subs += credentials.updateDataAsync { Single.fromCallable { it.copy(sid = newSid) } }
                                .ignoreElement().onErrorComplete().subscribe()
                    }
        } catch (e: Exception) {

        }
    }

    private fun checkIf403(response: Response) {
        if (!response.isSuccessful && response.code == HTTP_FORBIDDEN) {
            sessionRelay.accept(Unit)
        }
    }

    companion object {
        const val SailsSid = "sails.sid"
        const val CfDuid = "__cfduid"
    }
}