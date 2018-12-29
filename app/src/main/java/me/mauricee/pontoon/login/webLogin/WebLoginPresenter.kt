package me.mauricee.pontoon.login.webLogin

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.User
import me.mauricee.pontoon.login.LoginNavigator
import java.net.URLDecoder
import javax.inject.Inject

class WebLoginPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                            private val accountManagerHelper: AccountManagerHelper,
                                            private val navigator: LoginNavigator,
                                            eventTracker: EventTracker) : BasePresenter<WebLoginContract.State, WebLoginContract.View>(eventTracker) {

    override fun onViewAttached(view: WebLoginContract.View): Observable<WebLoginContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }
                    .flatMap<WebLoginContract.State> {
                        when (it) {
                            is WebLoginContract.Action.Login -> attemptLogin(it.cookies)
                        }
                    }.onErrorReturnItem(WebLoginContract.State.Error)

    private fun attemptLogin(cookieStr: String): Observable<WebLoginContract.State>? {
        val cookies = cookieStr.split(";").associate { cookie ->
            cookie.split("=").let { it.first() to it.last() }
        }
        val cfuIdKey = cookies.keys.first { it.contains(AuthInterceptor.CfDuid) }
        val sailsKey = cookies.keys.first { it.contains(AuthInterceptor.SailsSid) }

        accountManagerHelper.login(cookies[cfuIdKey]!!, URLDecoder.decode(cookies[sailsKey], "UTF-8")!!)
        return floatPlaneApi.self.flatMap(::onSuccessfulLogin)
    }

    private fun onSuccessfulLogin(user: User) = stateless {
        accountManagerHelper.account = user
        navigator.onSuccessfulLogin()
    }
}