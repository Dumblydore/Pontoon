package me.mauricee.pontoon.ui.login

import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(p: LoginPresenter) : EventViewModel<LoginState, LoginAction, LoginEvent>(LoginState(), p)

data class LoginState(val uiState: UiState = UiState.Empty,
                      val prompt2FaCode: Boolean = false)


sealed class LoginReducer {
    object Loading : LoginReducer()
    object Requires2Fa : LoginReducer()
    data class DisplayError(val error: LoginError) : LoginReducer()
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

sealed class LoginEvent {
    object NavigateToSession : LoginEvent()
    object NavigateToLttLogin : LoginEvent()
    object NavigateToDiscordLogin : LoginEvent()
    object NavigateToSignUp : LoginEvent()
    object NavigateToPrivacyPolicy : LoginEvent()
}


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

private const val CallbackPath = "/connect/login/callback"

enum class LoginWebsites(val url: String) {
    Ltt("https://www.floatplane.com/api/connect/ltt?redirect=$CallbackPath&create=true&login=true"),
    Discord("https://www.floatplane.com/api/connect/discord?redirect=$CallbackPath&create=true&login=true")
}