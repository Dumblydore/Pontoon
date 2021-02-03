package me.mauricee.pontoon.ui.login.webLogin

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.net.toUri
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.navArgs
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_web_login.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.login.LoginAction
import me.mauricee.pontoon.ui.login.LoginViewModel


@AndroidEntryPoint
class WebLoginFragment : NewBaseFragment(R.layout.fragment_web_login) {

    private val args: WebLoginFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by hiltNavGraphViewModels(R.id.login_graph)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebview()
        login_webview.loadUrl(args.site.url)
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
        viewModel.state.mapDistinct { it.uiState.error }.notNull().observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.text(requireContext()), Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        }
    }

    inner class Webclient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.settings.domStorageEnabled = !request.url.host!!.contains("floatplane")
            return super.shouldOverrideUrlLoading(view, request)
        }


        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            url.toUri().path?.with { uriPath ->
                if (uriPath.contains(CallbackPath) || uriPath.contains(ConfirmPath)) {
                    viewModel.sendAction(LoginAction.LoginWithCookie(CookieManager.getInstance().getCookie(url)))
                }
            }
        }
    }

    companion object {
        private const val CallbackPath = "/connect/login/callback"
        private const val ConfirmPath = "api/activation/email/confirm"
    }
}