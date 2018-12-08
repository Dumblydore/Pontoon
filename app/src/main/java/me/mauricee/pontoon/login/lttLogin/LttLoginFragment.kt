package me.mauricee.pontoon.login.lttLogin

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jakewharton.rxrelay2.PublishRelay
import kotlinx.android.synthetic.main.fragment_ltt_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import javax.inject.Inject

class LttLoginFragment : BaseFragment<LttPresenter>(), LttLoginContract.View {

    @Inject
    lateinit var interceptor: AuthInterceptor

    override fun getLayoutId(): Int = R.layout.fragment_ltt_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CookieManager.getInstance().setCookie("linustechtips.com", "__cfduid=${interceptor.cfduid}; sails.sid=${interceptor.sid}")

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
        val actionsRelay = PublishRelay.create<LttLoginContract.Action>()

        init {

        }


        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.loadUrl(request.url.toString())
            return false
        }

//        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
//            request.requestHeaders?.forEach { (key, value) -> logd("key: $key | value: $value") }
//            return null
//        }
    }
}