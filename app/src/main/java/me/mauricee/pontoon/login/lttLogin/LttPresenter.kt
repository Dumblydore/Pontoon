package me.mauricee.pontoon.login.lttLogin

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.login.LoginNavigator
import javax.inject.Inject

class LttPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                       private val accountManagerHelper: AccountManagerHelper,
                                       private val navigator: LoginNavigator,
                                       eventTracker: EventTracker) :
        BasePresenter<LttLoginContract.State, LttLoginContract.View>(eventTracker) {

    override fun onViewAttached(view: LttLoginContract.View): Observable<LttLoginContract.State> =
            view.actions.flatMap<LttLoginContract.State> {
                when (it) {
                    is LttLoginContract.Action.Login -> floatPlaneApi.self.flatMap { user ->
                                stateless {
                                    accountManagerHelper.account = user.user
                                    navigator.toSubscriptions()
                                }
                            }
                }
            }
}