package me.mauricee.pontoon.login.lttLogin

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_ltt_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R


class LttLoginFragment : BaseFragment<LttPresenter>(), LttLoginContract.View {
    private val actionsRelay = PublishRelay.create<LttLoginContract.Action>()
    override val actions: Observable<LttLoginContract.Action>
        get() = actionsRelay.hide()

    override fun getLayoutId(): Int = R.layout.fragment_ltt_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CookieManager.getInstance().apply {
            setAcceptThirdPartyCookies(login_webview, true)
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
    }

    inner class Webclient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            if (url.toUri().path.contains("connect/login/callback")) {
                actionsRelay.accept(LttLoginContract.Action.Login(url, CookieManager.getInstance().getCookie(url)))
            }
        }
    }
}