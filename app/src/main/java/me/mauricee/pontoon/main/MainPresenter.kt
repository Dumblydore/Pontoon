package me.mauricee.pontoon.main

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class MainPresenter @Inject constructor(private val accountManagerHelper: AccountManagerHelper,
                                        private val animationTouchListener: VideoTouchHandler,
                                        private val userRepository: UserRepository,
                                        private val videoRepository: VideoRepository,
                                        private val player: Player,
                                        private val pontoonDatabase: PontoonDatabase,
                                        private val navigator: MainContract.Navigator,
                                        eventTracker: EventTracker) :
        BasePresenter<MainContract.State, MainContract.View>(eventTracker), MainContract.Presenter {

    override fun onViewAttached(view: MainContract.View): Observable<MainContract.State> = Observable.merge(videoRepository.subscriptions
            .onErrorReturnItem(emptyList()).map { MainContract.State.CurrentUser(userRepository.activeUser, it.size) },
            actions(view))

    private fun actions(view: MainContract.View) = view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap {
        when (it) {
            is MainContract.Action.Logout -> Observable.fromCallable(::logout).compose(RxHelpers.applyObservableSchedulers())
            is MainContract.Action.Profile -> stateless { navigator.toUser(userRepository.activeUser) }
            is MainContract.Action.Preferences -> MainContract.State.Preferences.toObservable()
            is MainContract.Action.PlayerClicked -> stateless {
                if (!animationTouchListener.isExpanded) animationTouchListener.isExpanded = true
                else player.toggleControls()

            }
        }
    }

    private fun logout(): MainContract.State {
        accountManagerHelper.logout()
        pontoonDatabase.clearAllTables()
        return MainContract.State.Logout
    }
}