package me.mauricee.pontoon.login.login

import androidx.core.view.isVisible
import androidx.core.widget.toast
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_login.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.login.login.LoginContract.State.Error.Type.*

class LoginFragment : BaseFragment<LoginPresenter>(), LoginContract.View {

    override val actions: Observable<LoginContract.Action>
        get() = login_login.clicks().map { LoginContract.Action.Login(login_username_edit.text.toString(), login_password_edit.text.toString()) }
//                .doOnNext<LoginContract.Action>{eventTracker.trackAction(it, this)}



    override fun getLayoutId(): Int = R.layout.fragment_login

    override fun updateState(state: LoginContract.State) {
        when (state) {
            is LoginContract.State.Success -> requireActivity().toast("Success!")
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