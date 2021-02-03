package me.mauricee.pontoon.ui.main

import androidx.appcompat.app.AppCompatDelegate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.session.SessionRepository
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.ReduxPresenter
import javax.inject.Inject

class MainPresenter @Inject constructor(private val sessionRepository: SessionRepository,
                                        private val userRepository: UserRepository,
                                        private val subscriptionRepository: SubscriptionRepository,
                                        private val floatPlaneApi: FloatPlaneApi,
                                        private val pontoonDatabase: PontoonDatabase,
                                        private val authInterceptor: AuthInterceptor) : ReduxPresenter<MainContract.State, MainContract.Reducer, MainContract.Action, MainContract.Event>() {

    override fun onViewAttached(view: BaseContract.View<MainContract.State, MainContract.Action>): Observable<MainContract.Reducer> {
        return userRepository.activeUser.switchMap { user ->
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
        MainContract.Action.Preferences -> noReduce { sendEvent(MainContract.Event.NavigateToPreferences) }
        MainContract.Action.Profile -> noReduce { sendEvent(MainContract.Event.NavigateToUser(user)) }
        MainContract.Action.NightMode -> noReduce { sendEvent(MainContract.Event.TriggerNightMode(AppCompatDelegate.MODE_NIGHT_YES)) }
        MainContract.Action.PlayerClicked -> noReduce { }
        MainContract.Action.ToggleMenu -> noReduce { sendEvent(MainContract.Event.ToggleMenu) }
    }

    private fun getUser(user: User): Observable<MainContract.Reducer> = subscriptionRepository.subscriptions.get()
            .map(List<Creator>::size).onErrorReturnItem(0)
            .map { MainContract.Reducer.DisplayUser(user, it) }

    private fun sessionExpirations(): Observable<MainContract.Reducer> = authInterceptor.sessionExpired
            .concatMap { noReduce { sendEvent(MainContract.Event.SessionExpired) } }


    //        BasePresenter<MainContract.State, MainContract.View>(eventTracker), MainContract.Presenter {
//
//    override fun onViewAttached(view: MainContract.View): Observable<MainContract.State> =
//            Observable.merge(subscriptions(), actions(view), authInterceptor.sessionExpired.map { MainContract.State.SessionExpired })
//                    .startWith(MainContract.State.NightMode(false))
//
//    private fun actions(view: MainContract.View) = view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap {
//        when (it) {
//            is MainContract.Action.SuccessfulLogout -> floatPlaneApi.logout().onErrorComplete().andThen(logout())
//            is MainContract.Action.Profile -> userRepository.activeUser.firstElement().flatMapObservable { stateless { navigator.toUser(it.entity.id) } }
//            is MainContract.Action.Preferences -> stateless { navigator.toPreferences() }
//            is MainContract.Action.PlayerClicked -> toggleControls()
//            is MainContract.Action.PlayVideo -> playVideo(it)
//            MainContract.Action.Expired -> logout()
//            MainContract.Action.NightMode -> stateless {
//                navigator.setMenuExpanded(false)
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
////                themeManager.toggleNightMode()
//            }
//        }
//    }
//
//    private fun toggleControls(): Observable<MainContract.State> {
//        return stateless {
//            if (!animationTouchListener.isExpanded) animationTouchListener.isExpanded = true
//        }
//    }
//
//    private fun playVideo(it: MainContract.Action.PlayVideo) = stateless { navigator.playVideo(it.videoId) }
//
//    private fun subscriptions() = RxTuple.combineLatestAsPair(userRepository.activeUser,
//            subscriptionRepository.subscriptions.get().onErrorReturnItem(emptyList()))
//            .map {
//                val (user, subs) = it
//                MainContract.State.CurrentUser(user, subs.size)
//            }
//
    private fun logout() = Completable.merge(listOf(
            sessionRepository.logout(),
            Completable.fromAction { pontoonDatabase.clearAllTables() }
    )).andThen(noReduce { sendEvent(MainContract.Event.NavigateToLoginScreen) }).subscribeOn(Schedulers.io())
}