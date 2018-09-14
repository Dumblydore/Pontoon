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
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class Error(val type: Type = Type.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            enum class Type(@StringRes val msg: Int) {
                MissingUsername(R.string.login_error_missingUsername),
                MissingPassword(R.string.login_error_missingPassword),
                Credentials(R.string.login_error_credentials),
                Network(R.string.login_error_network),
                Service(R.string.login_error_service),
                General(R.string.login_error_general)
            }
        }

    }

}