package me.mauricee.pontoon.ui.login

import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.session.LoginResult
import me.mauricee.pontoon.model.session.SessionRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.ReduxPresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import retrofit2.HttpException
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val sessionRepository: SessionRepository) : ReduxPresenter<LoginState, LoginReducer, LoginAction, LoginEvent>() {

    private val codeRegex by lazy { Regex("[0-9]+") }

    override fun onViewAttached(view: BaseContract.View<LoginState, LoginAction>): Observable<LoginReducer> {
        return view.actions.flatMap { action ->
            when (action) {
                is LoginAction.Login -> attemptLogin(action.username, action.password)
                is LoginAction.LoginWithCookie -> loginWithCookie(action.cookie)
                is LoginAction.Activate -> attemptActivation(action.code, action.username)
                is LoginAction.Authenticate -> attemptAuthentication(action.authCode)
                LoginAction.LttLogin -> noReduce { sendEvent(LoginEvent.NavigateToLttLogin) }
                LoginAction.DiscordLogin -> noReduce { sendEvent(LoginEvent.NavigateToDiscordLogin) }
                LoginAction.SignUp -> noReduce { sendEvent(LoginEvent.NavigateToSignUp) }
                LoginAction.PrivacyPolicy -> noReduce { sendEvent(LoginEvent.NavigateToPrivacyPolicy) }
            }
        }
    }

    override fun onReduce(state: LoginState, reducer: LoginReducer): LoginState = when (reducer) {
        LoginReducer.Loading -> state.copy(uiState = UiState.Loading)
        LoginReducer.Requires2Fa -> state.copy(uiState = UiState.Success, prompt2FaCode = true)
        is LoginReducer.DisplayError -> state.copy(uiState = UiState.Failed(UiError(reducer.error.msg)))
    }

    private fun attemptLogin(username: String, password: String): Observable<LoginReducer> = Observable.defer {
        when {
            username.isEmpty() -> LoginReducer.DisplayError(LoginError.MissingUsername).toObservable()
            password.isEmpty() -> LoginReducer.DisplayError(LoginError.MissingUsername).toObservable()
            else -> loginWithCredentials(username, password)
        }
    }

    private fun loginWithCredentials(username: String, password: String): Observable<LoginReducer> {
        return sessionRepository.loginWithCredentials(username, password)
                .flatMapObservable(::processLoginResult)
                .startWith(LoginReducer.Loading)
    }

    private fun loginWithCookie(cookieStr: String) = Single.defer {
        val cookies = cookieStr.split(";").associate { cookie ->
            cookie.split("=").let { it.first() to it.last() }
        }
        val cfuIdKey = cookies.keys.first { it.contains(AuthInterceptor.CfDuid) }
        val sailsKey = cookies.keys.first { it.contains(AuthInterceptor.SailsSid) }
        sessionRepository.loginWithCookie(cfuIdKey, sailsKey)
    }.flatMapObservable(::processLoginResult).startWith(LoginReducer.Loading)

    private fun attemptAuthentication(code: String): Observable<LoginReducer> = if (code.matches(codeRegex)) {
        sessionRepository.authenticate(code).flatMapObservable(::processLoginResult).startWith(LoginReducer.Loading)
    } else Observable.just(LoginReducer.DisplayError(LoginError.InvalidAuthCode))

    private fun attemptActivation(code: String, username: String): Observable<LoginReducer> {
        return sessionRepository.activate(username, code).flatMapObservable(::processLoginResult)
                .startWith(LoginReducer.Loading)
    }

    private fun processLoginResult(result: LoginResult): Observable<LoginReducer> = when (result) {
        LoginResult.Requires2FA -> LoginReducer.Requires2Fa.toObservable()
        LoginResult.Success -> noReduce { sendEvent(LoginEvent.NavigateToSession) }
        is LoginResult.Error -> processError(result.exception).toObservable()
    }

    private fun processError(error: Throwable): LoginReducer = when (error) {
        is HttpException -> processHttpCode(error.code())
        else -> LoginReducer.DisplayError(LoginError.General)
    }

    private fun processHttpCode(code: Int): LoginReducer = when (code) {
        in 400..499 -> LoginError.NetworkCredentials
        in 500..599 -> LoginError.NetworkService
        else -> LoginError.NetworkUnknown
    }.let(LoginReducer::DisplayError)
}