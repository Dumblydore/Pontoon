package me.mauricee.pontoon.ui.login

import androidx.annotation.StringRes
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ui.BaseViewModel
import javax.inject.Inject

class LoginViewModel(p: NewLoginPresenter) : BaseViewModel<LoginState, LoginAction>(LoginState(), p) {
    class Factory @Inject constructor(p: NewLoginPresenter) : BaseViewModel.Factory<LoginViewModel>({ LoginViewModel(p) })
}

sealed class LoginAction : EventTracker.Action {
    data class Login(val username: String, val password: String) : LoginAction()
    data class LoginWithCookie(val cookie: String) : LoginAction()
    data class Activate(val code: String, val username: String) : LoginAction()
    data class Authenticate(val authCode: String) : LoginAction()
    object LttLogin : LoginAction()
    object DiscordLogin : LoginAction()
    object SignUp : LoginAction()
    object PrivacyPolicy : LoginAction()
}

data class LoginState(val isLoading: Boolean = false,
                      val prompt2FaCode: Boolean = false,
                      val error: LoginError? = null)


enum class LoginError(@StringRes val msg: Int) {
    MissingUsername(R.string.login_error_missingUsername),
    MissingPassword(R.string.login_error_missingPassword),
    Activation(R.string.login_error_network_activation),
    General(R.string.login_error_general),
    InvalidAuthCode(R.string.login_error_invalid_authCode),
    NetworkConnection(R.string.login_error_network_connection),
    NetworkCredentials(R.string.login_error_network_credentials),
    NetworkService(R.string.login_error_network_service),
    NetworkUnknown(R.string.login_error_network_unknown)
}