package me.mauricee.pontoon.preferences.settings

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.edge.EdgeRepository
import me.mauricee.pontoon.preferences.PreferencesNavigator
import javax.inject.Inject

class SettingsPresenter @Inject constructor(private val navigator: PreferencesNavigator,
                                            private val edgeRepository: EdgeRepository,
                                            eventTracker: EventTracker) :
        BasePresenter<SettingsContract.State, SettingsContract.View>(eventTracker) {

    override fun onViewAttached(view: SettingsContract.View): Observable<SettingsContract.State> = view.actions.doOnNext { eventTracker.trackAction(it, view) }
            .flatMap(this::handleActions)
            .onErrorResumeNext(Observable.empty())

    private fun handleActions(action: SettingsContract.Action): Observable<SettingsContract.State> = when (action) {
        is SettingsContract.Action.OpenBaseThemePreference -> SettingsContract.State.DisplayBaseThemePreference(action.key).toObservable()
        is SettingsContract.Action.OpenAccentColorPreference -> SettingsContract.State.DisplayAccentColorPreference(action.key).toObservable()
        is SettingsContract.Action.OpenPrimaryColorPreference -> SettingsContract.State.DisplayPrimaryColorPreference(action.key).toObservable()
        SettingsContract.Action.SelectedAbout -> stateless { navigator.toAbout() }
        SettingsContract.Action.SelectedPrivacyPolicy -> stateless { navigator.toPrivacyPolicy() }
        SettingsContract.Action.SelectedRefreshEdges -> edgeRepository.refresh()
                .andThen(Observable.just<SettingsContract.State>(SettingsContract.State.RefreshedEdges))
                .onErrorReturnItem(SettingsContract.State.ErrorRefreshingEdges)
                .startWith(SettingsContract.State.RefreshingEdges)
    }
}