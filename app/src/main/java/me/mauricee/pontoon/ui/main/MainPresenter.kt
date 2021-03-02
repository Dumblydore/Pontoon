package me.mauricee.pontoon.ui.main

import androidx.appcompat.app.AppCompatDelegate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.session.SessionRepository
import me.mauricee.pontoon.repository.subscription.SubscriptionRepository
import me.mauricee.pontoon.repository.user.User
import me.mauricee.pontoon.repository.util.AuthInterceptor
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import javax.inject.Inject

class MainPresenter @Inject constructor(private val sessionRepository: SessionRepository,
                                        private val subscriptionRepository: SubscriptionRepository,
                                        private val floatPlaneApi: FloatPlaneApi,
                                        private val player: Player,
                                        private val authInterceptor: AuthInterceptor) : BasePresenter<MainContract.State, MainContract.Reducer, MainContract.Action, MainContract.Event>() {

    override fun onViewAttached(view: BaseContract.View<MainContract.Action>): Observable<MainContract.Reducer> {
        return sessionRepository.activeUser.flatMapObservable { user ->
            Observable.merge(getUser(user), view.actions.flatMap { handleActions(user, it) })
        }.mergeWith(sessionExpirations())

    }

    override fun onReduce(state: MainContract.State, reducer: MainContract.Reducer): MainContract.State = when (reducer) {
        is MainContract.Reducer.DisplayUser -> state.copy(user = reducer.user, subCount = reducer.subCount)
        is MainContract.Reducer.DisplayNightModeToggle -> state.copy(isNightModeEnabled = reducer.isNightModeEnabled)
    }

    private fun handleActions(user: User, action: MainContract.Action): Observable<MainContract.Reducer> = when (action) {
        MainContract.Action.Expired -> logout()
        MainContract.Action.SuccessfulLogout -> floatPlaneApi.logout().onErrorComplete().andThen(logout())
        MainContract.Action.Preferences -> player.pause().andThen(noReduce { sendEvent(MainContract.Event.NavigateToPreferences) })
        MainContract.Action.Profile -> noReduce { sendEvent(MainContract.Event.NavigateToUser(user)) }
        MainContract.Action.NightMode -> noReduce { sendEvent(MainContract.Event.TriggerNightMode(AppCompatDelegate.MODE_NIGHT_YES)) }
        MainContract.Action.PlayerClicked -> noReduce { }
        MainContract.Action.ToggleMenu -> noReduce { sendEvent(MainContract.Event.ToggleMenu) }
    }

    private fun getUser(user: User): Observable<MainContract.Reducer> = subscriptionRepository.subscriptions.get()
            .toObservable()
            .map(List<Creator>::size).onErrorReturnItem(0)
            .map { MainContract.Reducer.DisplayUser(user, it) }

    private fun sessionExpirations(): Observable<MainContract.Reducer> = authInterceptor.sessionExpired
            .concatMap { noReduce { sendEvent(MainContract.Event.SessionExpired) } }

    private fun logout() = Completable.merge(listOf(
            player.stop(),
            sessionRepository.logout()
    )).andThen(noReduce { sendEvent(MainContract.Event.NavigateToLoginScreen) }).subscribeOn(Schedulers.io())
}