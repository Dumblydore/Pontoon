package me.mauricee.pontoon.login.lttLogin

import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.core.net.toUri
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_ltt_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.ext.logd
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject


class LttLoginFragment : BaseFragment<LttPresenter>(), LttLoginContract.View {

    @Inject
    lateinit var interceptor: AuthInterceptor
    @Inject
    lateinit var client: OkHttpClient;

    private val actionsRelay = PublishRelay.create<LttLoginContract.Action>()
    override val actions: Observable<LttLoginContract.Action>
        get() = actionsRelay.hide()

    override fun getLayoutId(): Int = R.layout.fragment_ltt_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headers = "__cfduid=${interceptor.cfduid}; sails.sid=${interceptor.sid}"
        logd(headers)
        CookieManager.getInstance().apply {
            setAcceptThirdPartyCookies(login_webview, true)
            setCookie("floatplane.com", "__cfduid=${interceptor.cfduid}; sails.sid=${interceptor.sid}")
        }
        WebView.setWebContentsDebuggingEnabled(true)

        login_webview.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
        }
        login_webview.webViewClient = Webclient()
        login_webview.loadUrl("https://www.floatplane.com/api/connect/ltt?redirect=/connect/login/callback&create=true&login=true")
    }

    override fun updateState(state: LttLoginContract.State) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class Webclient : WebViewClient() {

        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
            logd("loading url: ${request.url}")
            return if (request.url.path.contains("api/connect/ltt")) {
                client.newCall(Request.Builder()
                        .header("Cookie", "__cfduid=${interceptor.cfduid}; sails.sid=${interceptor.sid}")
                        .url(request.url.toString()).build())
                        .execute().let { response ->
                            WebResourceResponse(response.header("content-type", ""),
                                    response.header("content-encoding", "utf-8"),
                                    response.body()?.byteStream())
                        }
            } else {
                super.shouldInterceptRequest(view, request)
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            logd("current url: a$url")
            if (url.toUri().path.contains("connect/login/callback")) {
                logd("Successfully logged in!")
                actionsRelay.accept(LttLoginContract.Action.Login(url))
            }
        }

        //        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
//            request.requestHeaders?.forEach { (key, value) -> logd("key: $key | value: $value") }
//            return null
//        }
    }
}