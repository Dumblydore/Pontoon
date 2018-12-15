package me.mauricee.pontoon.login.login

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.LoginRequest
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                         private val manager: AccountManagerHelper,
                                         private val navigator: LoginNavigator,
                                         eventTracker: EventTracker) :
        LoginContract.Presenter, BasePresenter<LoginContract.State, LoginContract.View>(eventTracker) {

    override fun onViewAttached(view: LoginContract.View): Observable<LoginContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }
                    .flatMap(this::handleActions)
                    .onErrorReturnItem(LoginContract.State.Error())

    private fun handleActions(action: LoginContract.Action): Observable<LoginContract.State> = when (action) {
        is LoginContract.Action.Login -> attemptLogin(action.username, action.password)
        LoginContract.Action.LttLogin -> stateless(navigator::toLttLogin)
    }

    private fun attemptLogin(username: String, password: String): Observable<LoginContract.State> = when {
        username.isEmpty() -> LoginContract.State.Error(LoginContract.State.Error.Type.MissingUsername).toObservable()
        password.isEmpty() -> LoginContract.State.Error(LoginContract.State.Error.Type.MissingPassword).toObservable()
        else -> login(LoginRequest(username, password))
    }

    private fun login(request: LoginRequest): Observable<LoginContract.State> = floatPlaneApi.login(request)
            .flatMap {
                stateless {
                    manager.account = it.user
                    navigator.toSubscriptions()
                }
            }
            .startWith(LoginContract.State.Loading)
            .onErrorReturn(::processError)

    private fun processError(error: Throwable): LoginContract.State.Error = when (error) {
        is HttpException -> processHttpCode(error.code())
        is SocketTimeoutException -> LoginContract.State.Error.Type.Service
        else -> LoginContract.State.Error.Type.General
    }.let(LoginContract.State::Error)

    private fun processHttpCode(code: Int) = when (code) {
        401 -> LoginContract.State.Error.Type.Credentials
        500 -> LoginContract.State.Error.Type.Service
        else -> LoginContract.State.Error.Type.Network
    }

}