package me.mauricee.pontoon.login.login

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatplaneClient
import me.mauricee.pontoon.domain.floatplane.LoginResult
import me.mauricee.pontoon.domain.floatplane.api.*
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val floatplaneClient: FloatplaneClient,
                                         private val floatplaneApi: FloatPlaneApi,
                                         private val manager: AccountManagerHelper,
                                         private val navigator: LoginNavigator,
                                         eventTracker: EventTracker) :
        LoginContract.Presenter, BasePresenter<LoginContract.State, LoginContract.View>(eventTracker) {

    private val codeRegex = Regex("[0-9]+")

    override fun onViewAttached(view: LoginContract.View): Observable<LoginContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }
                    .flatMap(this::handleActions)
                    .onErrorReturn { LoginContract.State.Error.Unknown(it.javaClass.simpleName) }

    private fun handleActions(action: LoginContract.Action): Observable<LoginContract.State> = when (action) {
        is LoginContract.Action.Login -> attemptLogin(action.username, action.password)
        is LoginContract.Action.Activate -> attemptActivation(action.code, action.username)
        is LoginContract.Action.Authenticate -> attemptAuthentication(action.authCode)
        LoginContract.Action.LttLogin -> stateless(navigator::toLttLogin)
        LoginContract.Action.DiscordLogin -> stateless(navigator::toDiscordLogin)
        LoginContract.Action.SignUp -> stateless(navigator::toSignUp)
        LoginContract.Action.PrivacyPolicy -> stateless(navigator::toPrivacyPolicy)
    }

    private fun attemptAuthentication(code: String): Observable<LoginContract.State> = if (code.matches(codeRegex)) {
        floatplaneClient.check2FaLogin(code).flatMapObservable<LoginContract.State> { result ->
            when (result) {
                LoginResult.InvalidCredentials -> LoginContract.State.Error.Credentials.toObservable()
                LoginResult.Request2FA -> LoginContract.State.Request2FaCode.toObservable()
                is LoginResult.LoggedIn -> result.user.toObservable().flatMap(this::navigateToMain)
                is LoginResult.Error -> LoginContract.State.Error.Unknown(result.error.name).toObservable()
            }
        }.startWith(LoginContract.State.Loading)
    } else LoginContract.State.InvalidAuthCode.toObservable()

    private fun attemptActivation(code: String, username: String): Observable<LoginContract.State> =
            floatplaneApi.confirmEmail(ConfirmationRequest(code, username)).andThen(floatplaneApi.self)
                    .flatMap(this::navigateToMain)
                    .startWith(LoginContract.State.Loading)

    private fun attemptLogin(username: String, password: String): Observable<LoginContract.State> = when {
        username.isEmpty() -> LoginContract.State.Error.MissingUsername.toObservable()
        password.isEmpty() -> LoginContract.State.Error.MissingPassword.toObservable()
        else -> login(username, password)
    }

    private fun login(username: String, password: String): Observable<LoginContract.State> =
            floatplaneClient.login(username, password).flatMapObservable<LoginContract.State> { result ->
                when (result) {
                    LoginResult.InvalidCredentials -> LoginContract.State.Error.Credentials.toObservable()
                    LoginResult.Request2FA -> LoginContract.State.Request2FaCode.toObservable()
                    is LoginResult.LoggedIn -> result.user.toObservable().flatMap(this::navigateToMain)
                    is LoginResult.Error -> LoginContract.State.Error.Unknown(result.error.name).toObservable()
                }
            }.startWith(LoginContract.State.Loading)

    private fun navigateToMain(user: User) = stateless {
        manager.account = user
        navigator.onSuccessfulLogin()
    }

}