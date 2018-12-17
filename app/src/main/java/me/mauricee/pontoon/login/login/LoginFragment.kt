package me.mauricee.pontoon.login.login

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.R.id.login_alternativeLogin
import me.mauricee.pontoon.login.login.LoginContract.State.Error.Type.*

class LoginFragment : BaseFragment<LoginPresenter>(), LoginContract.View {

    override val actions: Observable<LoginContract.Action>
        get() = Observable.merge(login_lttForum.clicks().map { LoginContract.Action.LttLogin },
                login_lttForum.clicks().map { LoginContract.Action.DiscordLogin },
                login_login.clicks().map { LoginContract.Action.Login(login_username_edit.text.toString(), login_password_edit.text.toString()) })

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
        }
    }

    private fun displayLoadingState() {
        login_error.isVisible = false
        login_login.isEnabled = false
        login_login.text = ""
        login_progress.isVisible = true
    }

    private fun handleError(error: LoginContract.State.Error) {
        val msg = getString(error.type.msg)
        login_login.isEnabled = true
        login_progress.isVisible = false
        login_login.text = getString(R.string.login_login)
        when (error.type) {
            MissingUsername -> login_username_edit.error = msg
            MissingPassword -> login_password_edit.error = msg
            Credentials -> login_error.apply { isVisible = true; text = msg }
            Network -> login_error.apply { isVisible = true; text = msg }
            Service -> login_error.apply { isVisible = true; text = msg }
            General -> login_error.apply { isVisible = true; text = msg }
        }
    }

}