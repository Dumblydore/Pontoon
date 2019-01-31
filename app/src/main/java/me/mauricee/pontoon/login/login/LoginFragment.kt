package me.mauricee.pontoon.login.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import kotlinx.android.synthetic.main.fragment_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.toast
import me.mauricee.pontoon.login.login.LoginContract.State.Error.Type.*
import me.mauricee.pontoon.login.login.LoginContract.State.NetworkError.Type.*

class LoginFragment : BaseFragment<LoginPresenter>(), LoginContract.View {

    private val activation: String
        get() = arguments!!.getString(ActivationKey)
    private val username: String
        get() = arguments!!.getString(UsernameKey)

    override val actions: Observable<LoginContract.Action>
        get() = Observable.merge(login_lttForum.clicks().map { LoginContract.Action.LttLogin },
                login_discord.clicks().map { LoginContract.Action.DiscordLogin },
                login_signUp.clicks().map { LoginContract.Action.SignUp },
                login_login.clicks().map { LoginContract.Action.Login(login_username_edit.text.toString(), login_password_edit.text.toString()) })
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
            is LoginContract.State.NetworkError -> handleNetworkError(state)
        }
    }

    private fun displayLoadingState() {
        login_error.isVisible = false
        login_login.isEnabled = false
        login_login.text = ""
        login_progress.isVisible = true
    }

    private fun handleNetworkError(error: LoginContract.State.NetworkError) {
        login_progress.isVisible = false
        login_login.apply {
            text = getString(R.string.login_login)
            isEnabled = true
        }
        login_error.apply {
            isVisible = true
            text = getString(error.type.msg, error.code)
        }
    }

    private fun handleError(error: LoginContract.State.Error) {
        val msg = getString(error.type.msg)
        login_login.isEnabled = true
        login_progress.isVisible = false
        login_login.text = getString(R.string.login_login)
        when (error.type) {
            MissingUsername -> login_username_edit.error = msg
            MissingPassword -> login_password_edit.error = msg
            Network -> login_error.apply { isVisible = true; text = msg }
            General -> login_error.apply { isVisible = true; text = msg }
            Activation -> toast(msg)
        }
    }

    private fun emitActivationArgs(): ObservableTransformer<in LoginContract.Action, out LoginContract.Action> = ObservableTransformer {
        if (activation.isNotEmpty() && username.isNotEmpty())
            it.startWith(LoginContract.Action.Activate(activation, username))
        else
            it
    }

    companion object {
        private const val ActivationKey = "activation"
        private const val UsernameKey = "userName"

        fun newInstance(key: String, username: String): Fragment = LoginFragment().apply {
            arguments = bundleOf(ActivationKey to key, UsernameKey to username)
        }
    }

}