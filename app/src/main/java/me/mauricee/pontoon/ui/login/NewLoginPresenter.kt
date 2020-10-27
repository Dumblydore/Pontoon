package me.mauricee.pontoon.ui.login

import io.reactivex.Observable
import io.reactivex.functions.Function
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.*
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.StatefulPresenter
import retrofit2.HttpException
import java.net.URLDecoder
import javax.inject.Inject

class NewLoginPresenter @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                            private val manager: AccountManagerHelper,
                                            private val navigator: LoginNavigator) : StatefulPresenter<LoginState, LoginAction>() {

    private val codeRegex = Regex("[0-9]+")

    override fun onViewAttached(view: BaseContract.View<LoginState, LoginAction>): Observable<LoginState> {
        return view.actions.flatMap(::onAction)
    }

    private fun onAction(action: LoginAction): Observable<LoginState> = when (action) {
        is LoginAction.Login -> attemptLogin(action.username, action.password)
        is LoginAction.LoginWithCookie -> attemptLogin(action.cookie)
        is LoginAction.Activate -> attemptActivation(action.code, action.username)
        is LoginAction.Authenticate -> attemptAuthentication(action.authCode)
        LoginAction.LttLogin -> stateless(navigator::toLttLogin)
        LoginAction.DiscordLogin -> stateless(navigator::toDiscordLogin)
        LoginAction.SignUp -> stateless(navigator::toSignUp)
        LoginAction.PrivacyPolicy -> stateless(navigator::toPrivacyPolicy)
    }

    private fun attemptAuthentication(code: String): Observable<LoginState> = if (code.matches(codeRegex)) {
        floatPlaneApi.login(LoginAuthToken(code)).map(UserJson.Container::user)
                .flatMap(this::navigateToMain)
                .startWith(state.copy(isLoading = true))
                .onErrorReturn(::processError)
    } else state.copy(isLoading = false, error = LoginError.InvalidAuthCode).toObservable()

    private fun attemptActivation(code: String, username: String): Observable<LoginState> =
            floatPlaneApi.confirmEmail(ConfirmationRequest(code, username)).andThen(floatPlaneApi.self)
                    .flatMap(this::navigateToMain)
                    .startWith(state.copy(isLoading = true))
                    .onErrorReturnItem(state.copy(isLoading = false, error = LoginError.Activation))

    private fun attemptLogin(username: String, password: String): Observable<LoginState> = when {
        username.isEmpty() -> state.copy(isLoading = false, error = LoginError.MissingUsername).toObservable()
        password.isEmpty() -> state.copy(isLoading = false, error = LoginError.MissingPassword).toObservable()
        else -> login(LoginRequest(username, password))
    }

    private fun attemptLogin(cookieStr: String): Observable<LoginState> {
        val cookies = cookieStr.split(";").associate { cookie ->
            cookie.split("=").let { it.first() to it.last() }
        }
        val cfuIdKey = cookies.keys.first { it.contains(AuthInterceptor.CfDuid) }
        val sailsKey = cookies.keys.first { it.contains(AuthInterceptor.SailsSid) }

        manager.login(cookies[cfuIdKey]
                ?: "", URLDecoder.decode(cookies[sailsKey], "UTF-8")!!)

        return floatPlaneApi.self.flatMap(::navigateToMain).onErrorResumeNext(Function {
            stateless {
                if ((it as? HttpException)?.code() in 400..499) stateless { LoginState(prompt2FaCode = true) }
                else Observable.just(processError(it))
            }
        })
    }

    private fun login(request: LoginRequest): Observable<LoginState> = floatPlaneApi.login(request).flatMap {
        if (it.needs2Fa) state.copy(isLoading = false, prompt2FaCode = true).toObservable()
        else navigateToMain(it.user!!)
    }.startWith(state.copy(isLoading = true, error = null)).onErrorReturn(::processError)


    private fun navigateToMain(user: UserJson) = stateless {
        manager.account = user
        navigator.onSuccessfulLogin()
    }

    private fun processError(error: Throwable): LoginState = when (error) {
        is HttpException -> processHttpCode(error.code())
        else -> LoginState(error = LoginError.General)
    }

    private fun processHttpCode(code: Int): LoginState = when (code) {
        in 400..499 -> LoginError.NetworkCredentials
        in 500..599 -> LoginError.NetworkService
        else -> LoginError.NetworkUnknown
    }.let { LoginState(error = LoginError.General) }
}