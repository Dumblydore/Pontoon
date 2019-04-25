package me.mauricee.pontoon.login.login

import androidx.annotation.StringRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker

interface LoginContract {

    interface View : BaseContract.View<State, Action>, EventTracker.Page

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        data class Login(val username: String, val password: String) : Action()
        data class Activate(val code: String, val username: String) : Action()
        data class Authenticate(val authCode: String) : Action()
        object LttLogin : Action()
        object DiscordLogin : Action()
        object SignUp : Action()
        object PrivacyPolicy : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        object Request2FaCode : State()
        object InvalidAuthCode : State()
        sealed class Error(@StringRes val msg: Int) : State() {
            override val tag: String
                get() = javaClass.run { "${superclass.simpleName}_$simpleName" }
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            data class Unknown(val errorContext: String) : Error(R.string.login_error_network_unknown)
            object Credentials : Error(R.string.login_error_network_credentials)
            object Service : Error(R.string.login_error_network_service)
            object MissingUsername : Error(R.string.login_error_missingUsername)
            object MissingPassword : Error(R.string.login_error_missingPassword)
            object Network : Error(R.string.login_error_network_connection)
            object Activation : Error(R.string.login_error_network_activation)
            object General : Error(R.string.login_error_general)
        }
    }

}