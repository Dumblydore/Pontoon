package me.mauricee.pontoon.ui.login.login

import androidx.annotation.StringRes
import me.mauricee.pontoon.ui.BaseContract
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
        data class NetworkError(val type: Type, val code: Int) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            enum class Type(@StringRes val msg: Int) {
                Credentials(R.string.login_error_network_credentials),
                Service(R.string.login_error_network_service),
                Unknown(R.string.login_error_network_unknown)
            }
        }

        data class Error(val type: Type = Type.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            enum class Type(@StringRes val msg: Int) {
                MissingUsername(R.string.login_error_missingUsername),
                MissingPassword(R.string.login_error_missingPassword),
                Network(R.string.login_error_network_connection),
                Activation(R.string.login_error_network_activation),
                General(R.string.login_error_general)
            }
        }

    }

}