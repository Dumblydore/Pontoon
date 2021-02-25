package me.mauricee.pontoon.tv.login

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActionEvents
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.me.pontoon.feature.login.LoginAction
import me.mauricee.me.pontoon.feature.login.LoginEvent
import me.mauricee.me.pontoon.feature.login.LoginState
import me.mauricee.me.pontoon.feature.login.LoginViewModel
import me.mauricee.pontoon.tv.R
import me.mauricee.pontoon.tv.databinding.FragmentLoginBinding
import me.mauricee.pontoon.tv.login.LoginFragmentDirections.actionLoginFragmentToBrowseFragment
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.util.viewBinding.viewBinding

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private val binding by viewBinding(FragmentLoginBinding::bind)

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
        get() = Observable.merge(listOf(/*binding.loginLttForum.clicks().map { LoginAction.LttLogin },*/
//                binding.loginDiscord.clicks().map { LoginAction.DiscordLogin },
                binding.loginLogin.clicks().map { loginAction },
                binding.loginPasswordEdit.editorActionEvents().map { loginAction },
                binding.loginTokenEdit.editorActionEvents().map { loginAction },
//                binding.loginPrivacy.clicks().map { LoginAction.PrivacyPolicy }
        ))
//                .compose(emitActivationArgs())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().viewModelStore.clear()
        viewModel.events.observe(this) {
//            binding.root.hideKeyboard()
            when (it) {
                LoginEvent.NavigateToSession -> findNavController().navigate(actionLoginFragmentToBrowseFragment())
                LoginEvent.NavigateToLttLogin -> TODO()//findNavController().navigate(actionLoginFragmentToWebLoginFragment(LoginWebsites.Ltt))
                LoginEvent.NavigateToDiscordLogin -> TODO()//findNavController().navigate(actionLoginFragmentToWebLoginFragment(LoginWebsites.Discord))
                LoginEvent.NavigateToSignUp -> TODO()//toSignUp()
                LoginEvent.NavigateToPrivacyPolicy -> TODO()//toPrivacyPolicy()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscriptions += actions.subscribe(viewModel::sendAction)
        viewModel.state.map(LoginState::prompt2FaCode).observe(viewLifecycleOwner) {
            if (it) {
                binding.loginToken.isVisible = true
                binding.loginUsername.isVisible = false
                binding.loginPassword.isVisible = false
                binding.loginToken.requestFocus()
            }
        }
        viewModel.state.map(LoginState::uiState).observe(viewLifecycleOwner) {
            binding.loginError.isGone = it !is UiState.Failed
            when (it) {
                UiState.Loading -> displayLoadingState(true)
                UiState.Refreshing -> displayLoadingState(true)
                UiState.Empty -> displayLoadingState(false)
                UiState.Success -> displayLoadingState(false)
                is UiState.Failed -> handleError(it.error)
            }
        }
    }

    private fun displayLoadingState(isLoading: Boolean) {
        binding.loginLogin.isEnabled = !isLoading
        if (isLoading) {
//            binding.loginLogin.icon = loadingDrawable
//            binding.loginLogin.iconGravity = ExtendedFloatingActionButton.ICON_GRAVITY_END
            loadingDrawable.start()
//            binding.loginLogin.shrink()
        } else {
            loadingDrawable.stop()
//            binding.loginLogin.extend(listener)
        }
    }

    private fun handleError(error: UiError) {
        binding.loginError.text = error.text(requireContext())
        displayLoadingState(false)
    }


    private val listener = object : ExtendedFloatingActionButton.OnChangedCallback() {
        override fun onExtended(extendedFab: ExtendedFloatingActionButton) {
            extendedFab.icon = null
        }
    }
//    private fun emitActivationArgs(): ObservableTransformer<in LoginAction, out LoginAction> = ObservableTransformer {
//        if (activation.isNotEmpty() && username.isNotEmpty())
//            it.startWith(LoginAction.Activate(activation, username))
//        else
//            it
//    }
}