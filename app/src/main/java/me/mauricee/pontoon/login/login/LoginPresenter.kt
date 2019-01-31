package me.mauricee.pontoon.login.login

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.ConfirmationRequest
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.LoginRequest
import me.mauricee.pontoon.domain.floatplane.User
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import retrofit2.HttpException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
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
        is LoginContract.Action.Activate -> attemptActivation(action.code, action.username)
        LoginContract.Action.LttLogin -> stateless(navigator::toLttLogin)
        LoginContract.Action.DiscordLogin -> stateless(navigator::toDiscordLogin)
        LoginContract.Action.SignUp -> stateless(navigator::toSignUp)
    }

    private fun attemptActivation(code: String, username: String): Observable<LoginContract.State> =
            floatPlaneApi.confirmEmail(ConfirmationRequest(code, username)).andThen(floatPlaneApi.self)
                    .flatMap(this::navigateToMain)
                    .startWith(LoginContract.State.Loading)
                    .onErrorReturnItem(LoginContract.State.Error(LoginContract.State.Error.Type.Activation))

    private fun attemptLogin(username: String, password: String): Observable<LoginContract.State> = when {
        username.isEmpty() -> LoginContract.State.Error(LoginContract.State.Error.Type.MissingUsername).toObservable()
        password.isEmpty() -> LoginContract.State.Error(LoginContract.State.Error.Type.MissingPassword).toObservable()
        else -> login(LoginRequest(username, password))
    }

    private fun login(request: LoginRequest): Observable<LoginContract.State> = floatPlaneApi.login(request)
            .map(User.Container::user)
            .flatMap(this::navigateToMain)
            .startWith(LoginContract.State.Loading)
            .onErrorReturn(::processError)

    private fun navigateToMain(user: User) = stateless {
        manager.account = user
        navigator.onSuccessfulLogin()
    }

    private fun processError(error: Throwable): LoginContract.State = when (error) {
        is HttpException -> processHttpCode(error.code())
        else -> LoginContract.State.Error()
    }

    private fun processHttpCode(code: Int): LoginContract.State.NetworkError = when (code) {
        in 400..499 -> LoginContract.State.NetworkError.Type.Credentials
        in 500..599 -> LoginContract.State.NetworkError.Type.Service
        else -> LoginContract.State.NetworkError.Type.Unknown
    }.let { LoginContract.State.NetworkError(it, code) }

}