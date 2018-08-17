package me.mauricee.pontoon.main

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class MainPresenter @Inject constructor(private val accountManagerHelper: AccountManagerHelper,
                                        private val player: Player,
                                        private val navigator: MainContract.Navigator,
                                        eventTracker: EventTracker) :
        BasePresenter<MainContract.State, MainContract.View>(eventTracker), MainContract.Presenter {

    private val currentUser by lazy { accountManagerHelper.account.let { UserRepository.User(it.id, it.username, it.profileImage.path) } }

    override fun onViewAttached(view: MainContract.View): Observable<MainContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap {
                when (it) {
                    is MainContract.Action.Logout -> accountManagerHelper.logout().let { MainContract.State.Logout }.toObservable()
                    is MainContract.Action.Profile -> navigator.toUser(currentUser).let { MainContract.State.CurrentUser(currentUser) }.toObservable()
                    is MainContract.Action.Preferences -> MainContract.State.Preferences.toObservable()
                    is MainContract.Action.ClickEvent -> stateless { player.toggleControls() }
                }
            }.startWith(MainContract.State.CurrentUser(currentUser))
}