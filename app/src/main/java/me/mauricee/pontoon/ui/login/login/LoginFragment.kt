package me.mauricee.pontoon.ui.login.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_login.*
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.login.LoginAction
import me.mauricee.pontoon.ui.login.LoginError
import me.mauricee.pontoon.ui.login.LoginViewModel
import javax.inject.Inject

class LoginFragment : NewBaseFragment(R.layout.fragment_login) {

    @Inject
    lateinit var viewModel: LoginViewModel

    private val activation: String
        get() = arguments?.getString(ActivationKey) ?: ""
    private val username: String
        get() = arguments?.getString(UsernameKey) ?: ""

    private val loginAction: LoginAction
        get() = if (login_token.isVisible) LoginAction.Authenticate(login_token_edit.text.toString()) else
            LoginAction.Login(login_username_edit.text.toString(), login_password_edit.text.toString())

    private val actions: Observable<LoginAction>
        get() = Observable.merge(listOf(login_lttForum.clicks().map { LoginAction.LttLogin },
                login_discord.clicks().map { LoginAction.DiscordLogin },
                login_signUp.clicks().map { LoginAction.SignUp },
                login_login.clicks().map { loginAction },
                login_password_edit.editorActionEvents().map { loginAction },
                login_token_edit.editorActionEvents().map { loginAction },
                login_privacy.clicks().map { LoginAction.PrivacyPolicy }))
                .compose(emitActivationArgs())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            login_username_edit.setText(R.string.default_user)
            login_password_edit.setText(R.string.default_pass)
        }
        viewModel.watchStateValue { error }.observe(viewLifecycleOwner, ::handleError)
        viewModel.watchStateValue { isLoading }.observe(viewLifecycleOwner) { displayLoadingState(it) }
        viewModel.watchStateValue { prompt2FaCode }.observe(viewLifecycleOwner) {
            if (it) {
                login_token.isVisible = true
                login_username.isVisible = false
                login_password.isVisible = false
                login_token.requestFocus()
            }
        }
        subscriptions += actions.subscribe(viewModel::sendAction)
    }

    private fun displayLoadingState(isLoading: Boolean) {
        login_login.isEnabled = !isLoading
        login_login.text = if (isLoading) "" else getText(R.string.login_login)
        login_progress.isVisible = isLoading

    }

    private fun handleError(error: LoginError?) {
        login_error.isVisible = error?.let {
            login_error.text = getText(it.msg)
            true
        } ?: false
    }

    private fun emitActivationArgs(): ObservableTransformer<in LoginAction, out LoginAction> = ObservableTransformer {
        if (activation.isNotEmpty() && username.isNotEmpty())
            it.startWith(LoginAction.Activate(activation, username))
        else
            it
    }

    companion object {
        private const val ActivationKey = "activation"
        private const val UsernameKey = "username"

        fun newInstance(key: String, username: String): LoginFragment = LoginFragment().apply {
            arguments = bundleOf(ActivationKey to key, UsernameKey to username)
        }
    }

}