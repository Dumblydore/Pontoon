package me.mauricee.pontoon.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.databinding.FragmentLoginBinding
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.view.hideKeyboard
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.login.LoginFragmentDirections.actionGlobalMainFragment
import me.mauricee.pontoon.ui.login.LoginFragmentDirections.actionLoginFragmentToWebLoginFragment

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by hiltNavGraphViewModels(R.id.login_graph)
    private val binding by viewBinding(FragmentLoginBinding::bind)

    private val activation: String
        get() = arguments?.getString(ActivationKey) ?: ""
    private val username: String
        get() = arguments?.getString(UsernameKey) ?: ""

    private val loginAction: LoginAction
        get() = if (binding.loginToken.isVisible) LoginAction.Authenticate(binding.loginTokenEdit.text.toString()) else
            LoginAction.Login(binding.loginUsernameEdit.text.toString(), binding.loginPasswordEdit.text.toString())

    private val loadingDrawable by lazy {
        CircularProgressDrawable(requireContext()).apply {
            setColorSchemeColors(binding.loginLogin.currentTextColor)
            strokeWidth = 4f
        }
    }

    private val actions: Observable<LoginAction>
        get() = Observable.merge(listOf(binding.loginLttForum.clicks().map { LoginAction.LttLogin },
                binding.loginDiscord.clicks().map { LoginAction.DiscordLogin },
                binding.loginLogin.clicks().map { loginAction },
                binding.loginPasswordEdit.editorActionEvents().map { loginAction },
                binding.loginTokenEdit.editorActionEvents().map { loginAction },
                binding.loginPrivacy.clicks().map { LoginAction.PrivacyPolicy }))
                .compose(emitActivationArgs())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().viewModelStore.clear()
        viewModel.events.observe(this) {
            binding.root.hideKeyboard()
            when (it) {
                LoginEvent.NavigateToSession -> findNavController().navigate(actionGlobalMainFragment())
                LoginEvent.NavigateToLttLogin -> findNavController().navigate(actionLoginFragmentToWebLoginFragment(LoginWebsites.Ltt))
                LoginEvent.NavigateToDiscordLogin -> findNavController().navigate(actionLoginFragmentToWebLoginFragment(LoginWebsites.Discord))
                LoginEvent.NavigateToSignUp -> toSignUp()
                LoginEvent.NavigateToPrivacyPolicy -> toPrivacyPolicy()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            binding.loginUsernameEdit.setText(R.string.default_user)
            binding.loginPasswordEdit.setText(R.string.default_pass)
        }

        viewModel.watchStateValue { prompt2FaCode }.observe(viewLifecycleOwner) {
            if (it) {
                binding.loginToken.isVisible = true
                binding.loginUsername.isVisible = false
                binding.loginPassword.isVisible = false
                binding.loginToken.requestFocus()
            }
        }
        viewModel.state.mapDistinct(LoginState::uiState).observe(viewLifecycleOwner) {
            binding.loginError.isGone = it !is UiState.Failed
            when (it) {
                UiState.Loading -> displayLoadingState(true)
                UiState.Refreshing -> displayLoadingState(true)
                UiState.Empty -> displayLoadingState(false)
                UiState.Success -> displayLoadingState(false)
                is UiState.Failed -> handleError(it.error)
            }
        }

        subscriptions += actions.subscribe(viewModel::sendAction)
    }

    private fun displayLoadingState(isLoading: Boolean) {
        binding.loginLogin.isEnabled = !isLoading
        if (isLoading) {
            binding.loginLogin.icon = loadingDrawable
            binding.loginLogin.iconGravity = ExtendedFloatingActionButton.ICON_GRAVITY_END
            loadingDrawable.start()
            binding.loginLogin.shrink()
        } else {
            loadingDrawable.stop()
            binding.loginLogin.extend(listener)
        }
    }

    private fun handleError(error: UiError) {
        binding.loginError.text = error.text(requireContext())
        displayLoadingState(false)
    }

    private fun emitActivationArgs(): ObservableTransformer<in LoginAction, out LoginAction> = ObservableTransformer {
        if (activation.isNotEmpty() && username.isNotEmpty())
            it.startWith(LoginAction.Activate(activation, username))
        else
            it
    }

    private fun toSignUp() = requireActivity().startActivity(Intent(Intent.ACTION_VIEW, SignupUrl.toUri()))

    private fun toPrivacyPolicy() = requireActivity().startActivity(Intent(Intent.ACTION_VIEW, PrivacyManager.privacyPolicyUri))

    private val listener = object : ExtendedFloatingActionButton.OnChangedCallback() {
        override fun onExtended(extendedFab: ExtendedFloatingActionButton) {
            extendedFab.icon = null
        }
    }

    companion object {
        private const val SignupUrl = "https://www.floatplane.com/signup"
        private const val ActivationKey = "activation"
        private const val UsernameKey = "username"

        fun newInstance(key: String, username: String): LoginFragment = LoginFragment().apply {
            arguments = bundleOf(ActivationKey to key, UsernameKey to username)
        }
    }

}