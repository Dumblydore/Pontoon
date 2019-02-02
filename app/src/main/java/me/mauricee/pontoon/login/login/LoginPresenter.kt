package me.mauricee.pontoon.login.login

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.*
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import retrofit2.HttpException
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                         private val manager: AccountManagerHelper,
                                         private val navigator: LoginNavigator,
                                         eventTracker: EventTracker) :
        LoginContract.Presenter, BasePresenter<LoginContract.State, LoginContract.View>(eventTracker) {

    private val codeRegex = Regex("[0-9]+")

    override fun onViewAttached(view: LoginContract.View): Observable<LoginContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }
                    .flatMap(this::handleActions)
                    .onErrorReturnItem(LoginContract.State.Error())

    private fun handleActions(action: LoginContract.Action): Observable<LoginContract.State> = when (action) {
        is LoginContract.Action.Login -> attemptLogin(action.username, action.password)
        is LoginContract.Action.Activate -> attemptActivation(action.code, action.username)
        is LoginContract.Action.Authenticate -> attemptAuthentication(action.authCode)
        LoginContract.Action.LttLogin -> stateless(navigator::toLttLogin)
        LoginContract.Action.DiscordLogin -> stateless(navigator::toDiscordLogin)
        LoginContract.Action.SignUp -> stateless(navigator::toSignUp)
    }

    private fun attemptAuthentication(code: String): Observable<LoginContract.State> = if (code.matches(codeRegex)) {
        floatPlaneApi.login(LoginAuthToken(code)).map(User.Container::user)
                .flatMap(this::navigateToMain)
                .startWith(LoginContract.State.Loading)
                .onErrorReturn(::processError)
    } else LoginContract.State.InvalidAuthCode.toObservable()

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
            .flatMap<LoginContract.State> {
                if (it.needs2Fa) LoginContract.State.Request2FaCode.toObservable() else
                    it.user.toObservable()
                            .flatMap(this::navigateToMain)
            }.startWith(LoginContract.State.Loading).onErrorReturn(::processError)

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