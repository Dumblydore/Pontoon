package me.mauricee.pontoon.login.lttLogin

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.login.LoginNavigator
import java.net.URLDecoder
import javax.inject.Inject

class LttPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                       private val authInterceptor: AuthInterceptor,
                                       private val accountManagerHelper: AccountManagerHelper,
                                       private val navigator: LoginNavigator,
                                       eventTracker: EventTracker) :
        BasePresenter<LttLoginContract.State, LttLoginContract.View>(eventTracker) {

    override fun onViewAttached(view: LttLoginContract.View): Observable<LttLoginContract.State> =
            view.actions.flatMap<LttLoginContract.State> {
                when (it) {
                    is LttLoginContract.Action.Login -> {
                        getKeysFromCookie(it.cookies)
                        floatPlaneApi.self.flatMap { user ->
                            stateless {
                                logd("Successfully logged in!")
                                accountManagerHelper.account = user
                                navigator.toSubscriptions()
                            }
                        }
                    }
                }
            }

    private fun getKeysFromCookie(cookie: String) {
        val cookies = cookie.split(";").map { cookie ->
            cookie.split("=").let {
                it.first() to it.last()
            }
        }.toMap()
        val cfuIdKey = cookies.keys.first { it.contains("cfduid") }
        val sailsKey = cookies.keys.first { it.contains("sails") }
        authInterceptor.login(cookies[cfuIdKey]!!, URLDecoder.decode(cookies[sailsKey], "UTF-8")!!)
    }
}