package me.mauricee.pontoon.login.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActionEvents
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import kotlinx.android.synthetic.main.fragment_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.just
import me.mauricee.pontoon.ext.toast

class LoginFragment : BaseFragment<LoginPresenter>(), LoginContract.View {

    private val activation: String
        get() = arguments?.getString(ActivationKey) ?: ""
    private val username: String
        get() = arguments?.getString(UsernameKey) ?: ""

    private val loginAction: LoginContract.Action
        get() = if (login_token.isVisible) LoginContract.Action.Authenticate(login_token_edit.text.toString()) else
            LoginContract.Action.Login(login_username_edit.text.toString(), login_password_edit.text.toString())

    override val actions: Observable<LoginContract.Action>
        get() = Observable.merge(listOf(login_lttForum.clicks().map { LoginContract.Action.LttLogin },
                login_discord.clicks().map { LoginContract.Action.DiscordLogin },
                login_signUp.clicks().map { LoginContract.Action.SignUp },
                login_login.clicks().map { loginAction },
                login_password_edit.editorActionEvents().map { loginAction },
                login_token_edit.editorActionEvents().map { loginAction },
                login_privacy.clicks().map { LoginContract.Action.PrivacyPolicy }))
                .compose(emitActivationArgs())

    override fun getLayoutId(): Int = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.DEBUG) {
            login_username_edit.setText(R.string.default_user)
            login_password_edit.setText(R.string.default_pass)
        }
    }

    override fun updateState(state: LoginContract.State) {
        when (state) {
            is LoginContract.State.Error -> handleError(state)
            is LoginContract.State.Loading -> displayLoadingState()
            LoginContract.State.Request2FaCode -> {
                resetLoginButton()
                login_token.isVisible = true
                login_username.isVisible = false
                login_password.isVisible = false
                login_token.requestFocus()
            }
            LoginContract.State.InvalidAuthCode -> {
                resetLoginButton()
                login_error.apply { isVisible = true; text = getText(R.string.login_error_invalid_authCode) }
            }
        }
    }

    private fun displayLoadingState() {
        login_error.isVisible = false
        login_login.isEnabled = false
        login_login.text = ""
        login_progress.isVisible = true
    }

    //TODO bubble up network codes?
    private fun handleError(error: LoginContract.State.Error) {
        val msg = getString(error.msg)
        resetLoginButton()
        when (error) {
            LoginContract.State.Error.MissingUsername -> login_username_edit.error = msg
            LoginContract.State.Error.MissingPassword -> login_password_edit.error = msg
            LoginContract.State.Error.Network,
            LoginContract.State.Error.General,
            LoginContract.State.Error.Credentials,
            LoginContract.State.Error.Service -> login_error.apply {
                isVisible = true
                text = getString(error.msg, error)
            }
            is LoginContract.State.Error.Unknown -> login_error.apply {
                isVisible = true
                text = getString(error.msg, error.errorContext)
            }
            LoginContract.State.Error.Activation -> toast(msg)
        }
    }

    private fun emitActivationArgs(): ObservableTransformer<in LoginContract.Action, out LoginContract.Action> = ObservableTransformer {
        if (activation.isNotEmpty() && username.isNotEmpty())
            it.startWith(LoginContract.Action.Activate(activation, username))
        else
            it
    }

    private fun resetLoginButton() = login_login.just {
        isEnabled = true
        text = getString(R.string.login_login)
        login_progress.isVisible = false
    }

    companion object {
        private const val ActivationKey = "activation"
        private const val UsernameKey = "username"

        fun newInstance(key: String, username: String): LoginFragment = LoginFragment().apply {
            arguments = bundleOf(ActivationKey to key, UsernameKey to username)
        }
//        fun newInstance
    }

}