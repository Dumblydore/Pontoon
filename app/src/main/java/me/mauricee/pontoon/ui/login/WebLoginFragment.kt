package me.mauricee.pontoon.ui.login

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
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentWebLoginBinding
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.ui.BaseFragment


@AndroidEntryPoint
class WebLoginFragment : BaseFragment(R.layout.fragment_web_login) {

    private val args: WebLoginFragmentArgs by navArgs()
    private val viewModel: LoginViewModel by hiltNavGraphViewModels(R.id.login_graph)
    private val binding by viewBinding(FragmentWebLoginBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebview()
        binding.loginWebview.loadUrl(args.site.url)
        subscriptions += binding.loginToolbar.navigationClicks().subscribe { requireActivity().onBackPressed() }
    }

    private fun setupWebview() {
        CookieManager.getInstance().apply {
            setAcceptThirdPartyCookies(binding.loginWebview, true)
        }
        binding.loginWebview.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
        }
        binding.loginWebview.webViewClient = Webclient()
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