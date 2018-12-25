package me.mauricee.pontoon.login.webLogin

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_web_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.with


class WebLoginFragment : BaseFragment<WebLoginPresenter>(), WebLoginContract.View {
    private val actionsRelay: Relay<WebLoginContract.Action> = PublishRelay.create()
    override val actions: Observable<WebLoginContract.Action>
        get() = actionsRelay.hide()

    private val url: String by lazy { arguments!!.getString(UrlKey) }

    override fun getLayoutId(): Int = R.layout.fragment_web_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebview()
        login_webview.loadUrl(url)
        subscriptions += RxToolbar.navigationClicks(login_toolbar).subscribe { requireActivity().onBackPressed() }
    }

    private fun setupWebview() {
        CookieManager.getInstance().apply {
            setAcceptThirdPartyCookies(login_webview, true)
        }
        login_webview.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
        }
        login_webview.webViewClient = Webclient()
    }

    override fun updateState(state: WebLoginContract.State) = when (state) {
        is WebLoginContract.State.Error -> {
            Toast.makeText(requireContext(), getString(R.string.lttLogin_error), Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        }
    }

    inner class Webclient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            login_webview.settings.domStorageEnabled = !request.url.host.contains("floatplane")
            return super.shouldOverrideUrlLoading(view, request)
        }


        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            url.toUri().path?.with { uriPath ->
                if (uriPath.contains(CallbackPath) || uriPath.contains(ConfirmPath)) {
                    actionsRelay.accept(WebLoginContract.Action.Login(CookieManager.getInstance().getCookie(url)))
                }
            }
        }
    }

    companion object {

        private const val UrlKey = "Url_Key"
        private const val CallbackPath = "/connect/login/callback"
        private const val ConfirmPath = "api/activation/email/confirm"
        private const val LttUrl = "https://www.floatplane.com/api/connect/ltt?redirect=$CallbackPath&create=true&login=true"
        private const val DiscordUrl = "https://www.floatplane.com/api/connect/discord?redirect=$CallbackPath&create=true&login=true"

        fun loginWithLttForum(): Fragment = WebLoginFragment().apply { arguments = bundleOf(UrlKey to LttUrl) }
        fun loginWithDiscord(): Fragment = WebLoginFragment().apply { arguments = bundleOf(UrlKey to DiscordUrl) }
    }
}