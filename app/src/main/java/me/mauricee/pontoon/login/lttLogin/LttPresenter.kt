package me.mauricee.pontoon.login.lttLogin

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import javax.inject.Inject

class LttPresenter @Inject constructor(private val interceptor: AuthInterceptor, eventTracker: EventTracker) :
        BasePresenter<LttLoginContract.State, LttLoginContract.View>(eventTracker) {

    override fun onViewAttached(view: LttLoginContract.View): Observable<LttLoginContract.State> =
            view.actions.flatMap<LttLoginContract.State> {
                when (it) {
                    is LttLoginContract.Action.Login -> stateless { interceptor.login(it.cfid, it.sid) }
                }
            }
}