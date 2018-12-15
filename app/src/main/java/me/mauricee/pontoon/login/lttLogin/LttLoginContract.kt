package me.mauricee.pontoon.login.lttLogin

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker

interface LttLoginContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State
    sealed class Action : EventTracker.Action {
        data class Login(val respCode: String) : Action()
    }
}