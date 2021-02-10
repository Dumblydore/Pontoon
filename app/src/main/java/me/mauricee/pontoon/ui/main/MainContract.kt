package me.mauricee.pontoon.ui.main

import androidx.annotation.IdRes
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.ui.EventViewModel
import javax.inject.Inject

interface MainContract {

    data class State(val user: User? = null,
                     val subCount: Int? = null,
                     val isNightModeEnabled: Boolean = false)

    sealed class Reducer {
        data class DisplayUser(val user: User, val subCount: Int) : Reducer()
        data class DisplayNightModeToggle(val isNightModeEnabled: Boolean) : Reducer()
    }

    sealed class Action : EventTracker.Action {
        object SuccessfulLogout : Action()
        object Expired : Action()
        object Preferences : Action()
        object Profile : Action()
        object NightMode : Action()
        object PlayerClicked : Action()
        object ToggleMenu : Action()

        companion object {
            fun fromNavDrawer(@IdRes id: Int) = when (id) {
                R.id.action_logout -> SuccessfulLogout
                R.id.action_prefs -> Preferences
                R.id.action_profile -> Profile
                R.id.action_dayNight -> NightMode
                else -> throw RuntimeException("Invalid Navigation Drawer option")
            }
        }
    }

    sealed class Event {
        data class TriggerNightMode(val mode: Int) : Event()
        data class NavigateToUser(val user: User) : Event()
        object ToggleMenu : Event()
        object CloseMenu : Event()
        object NavigateToPreferences : Event()
        object NavigateToLoginScreen : Event()
        object SessionExpired : Event()
    }

    @HiltViewModel
    class ViewModel @Inject constructor(p: MainPresenter) : EventViewModel<State, Action, Event>(State(), p)
}