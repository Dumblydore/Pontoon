package me.mauricee.pontoon.login.webLogin

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.login.LoginNavigator
import java.net.URLDecoder
import javax.inject.Inject

class WebLoginPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                            private val accountManagerHelper: AccountManagerHelper,
                                            private val navigator: LoginNavigator,
                                            eventTracker: EventTracker) : BasePresenter<WebLoginContract.State, WebLoginContract.View>(eventTracker) {

    override fun onViewAttached(view: WebLoginContract.View): Observable<WebLoginContract.State> =
            view.actions.flatMap<WebLoginContract.State> {
                when (it) {
                    is WebLoginContract.Action.Login -> {
                        getKeysFromCookie(it.cookies)
                        floatPlaneApi.self.flatMap { user ->
                            stateless {
                                accountManagerHelper.account = user
                                navigator.toSubscriptions()
                            }
                        }
                    }
                }
            }.onErrorReturnItem(WebLoginContract.State.Error)

    private fun getKeysFromCookie(cookie: String) {
        val cookies = cookie.split(";").map { cookie ->
            cookie.split("=").let {
                it.first() to it.last()
            }
        }.toMap()
        val cfuIdKey = cookies.keys.first { it.contains("cfduid") }
        val sailsKey = cookies.keys.first { it.contains("sails") }
        accountManagerHelper.login(cookies[cfuIdKey]!!, URLDecoder.decode(cookies[sailsKey], "UTF-8")!!)
    }
}