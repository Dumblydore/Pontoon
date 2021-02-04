package me.mauricee.pontoon.ui.preferences.settings

import io.reactivex.Observable
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.ReduxPresenter
import javax.inject.Inject

class SettingsPresenter @Inject constructor() : ReduxPresenter<SettingsContract.State, SettingsContract.Reducer, SettingsContract.Action, SettingsContract.Event>() {

    override fun onViewAttached(view: BaseContract.View<SettingsContract.State, SettingsContract.Action>): Observable<SettingsContract.Reducer> {
        return view.actions.flatMap { action ->
            when (action) {
                SettingsContract.Action.SelectedAbout -> noReduce { sendEvent(SettingsContract.Event.NavigateToAbout) }
                SettingsContract.Action.SelectedPrivacyPolicy -> noReduce { sendEvent(SettingsContract.Event.NavigateToPrivacyPolicy) }
                is SettingsContract.Action.OpenAccentColorPreference -> noReduce { sendEvent(SettingsContract.Event.DisplayAccentColorPreference(action.key)) }
                is SettingsContract.Action.OpenPrimaryColorPreference -> noReduce { sendEvent(SettingsContract.Event.DisplayPrimaryColorPreference(action.key)) }
            }

        }
    }

    override fun onReduce(state: SettingsContract.State, reducer: SettingsContract.Reducer): SettingsContract.State = state

}