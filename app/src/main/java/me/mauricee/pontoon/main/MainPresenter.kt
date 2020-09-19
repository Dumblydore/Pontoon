package me.mauricee.pontoon.main

import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class MainPresenter @Inject constructor(private val accountManagerHelper: AccountManagerHelper,
                                        private val animationTouchListener: VideoTouchHandler,
                                        private val userRepository: UserRepository,
                                        private val videoRepository: VideoRepository,
                                        private val subscriptionRepository: SubscriptionRepository,
                                        private val player: Player,
                                        private val floatPlaneApi: FloatPlaneApi,
                                        private val pontoonDatabase: PontoonDatabase,
                                        private val authInterceptor: AuthInterceptor,
                                        private val themeManager: ThemeManager,
                                        private val navigator: MainContract.Navigator,
                                        eventTracker: EventTracker) :
        BasePresenter<MainContract.State, MainContract.View>(eventTracker), MainContract.Presenter {

    override fun onViewAttached(view: MainContract.View): Observable<MainContract.State> =
            Observable.merge(subscriptions(), actions(view), authInterceptor.sessionExpired.map { MainContract.State.SessionExpired })
                    .startWith(MainContract.State.NightMode(themeManager.isInNightMode))

    private fun actions(view: MainContract.View) = view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap {
        when (it) {
            is MainContract.Action.SuccessfulLogout -> floatPlaneApi.logout().onErrorComplete().andThen(logout())
            is MainContract.Action.Profile -> stateless { navigator.toUser(userRepository.activeUser) }
            is MainContract.Action.Preferences -> stateless { navigator.toPreferences() }
            is MainContract.Action.PlayerClicked -> toggleControls()
            is MainContract.Action.PlayVideo -> playVideo(it)
            MainContract.Action.Expired -> logout()
            MainContract.Action.NightMode -> stateless {
                navigator.setMenuExpanded(false)
                themeManager.toggleNightMode()
            }
        }
    }

    private fun toggleControls(): Observable<MainContract.State> {
        return stateless {
            if (!animationTouchListener.isExpanded) animationTouchListener.isExpanded = true
        }
    }

    private fun playVideo(it: MainContract.Action.PlayVideo) =
            videoRepository.getVideo(it.videoId).firstOrError().flatMapObservable { stateless { navigator.playVideo(it) } }

    private fun subscriptions() = subscriptionRepository.subscriptions.onErrorReturnItem(emptyList())
            .map { MainContract.State.CurrentUser(userRepository.activeUser, it.size) }

    private fun logout(): Observable<MainContract.State> = Completable.fromAction {
        accountManagerHelper.logout()
        pontoonDatabase.clearAllTables()
    }.doOnIo().onErrorComplete().andThen(Observable.just<MainContract.State>(MainContract.State.Logout))
}