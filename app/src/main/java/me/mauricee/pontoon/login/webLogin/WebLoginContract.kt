package me.mauricee.pontoon.login.webLogin

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker

interface WebLoginContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Error : State()
    }
    sealed class Action : EventTracker.Action {
        data class Login(val cookies: String) : Action()
    }
}