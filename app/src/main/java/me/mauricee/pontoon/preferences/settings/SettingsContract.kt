package me.mauricee.pontoon.preferences.settings

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker

interface SettingsContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object RefreshingEdges : State()
        object RefreshedEdges : State()
        object ErrorRefreshingEdges : State()
        class DisplayBaseThemePreference(val key: String): State()
        class DisplayAccentColorPreference(val key: String): State()
        class DisplayPrimaryColorPreference(val key: String): State()
    }
    sealed class Action : EventTracker.Action {
        object SelectedAbout : Action()
        object SelectedRefreshEdges : Action()
        class OpenBaseThemePreference(val key: String) : Action()
        class OpenAccentColorPreference(val key: String) : Action()
        class OpenPrimaryColorPreference(val key: String) : Action()
    }
}