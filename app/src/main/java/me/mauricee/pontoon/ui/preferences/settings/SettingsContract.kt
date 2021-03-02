package me.mauricee.pontoon.ui.preferences.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ui.BaseViewModel
import javax.inject.Inject

interface SettingsContract {
     class State

    sealed class Event {
        object NavigateToAbout : Event()
        object NavigateToPrivacyPolicy : Event()
        data class DisplayAccentColorPreference(val key: String) : Event()
        data class DisplayPrimaryColorPreference(val key: String) : Event()
    }

    sealed class Reducer

    sealed class Action : EventTracker.Action {
        object SelectedAbout : Action()
        object SelectedPrivacyPolicy : Action()
        data class OpenAccentColorPreference(val key: String) : Action()
        data class OpenPrimaryColorPreference(val key: String) : Action()
    }

    @HiltViewModel
    class ViewModel @Inject constructor(p: SettingsPresenter) : BaseViewModel<State, Action, Event>(State(), p)
}