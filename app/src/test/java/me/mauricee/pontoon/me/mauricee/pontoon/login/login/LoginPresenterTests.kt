package me.mauricee.pontoon.me.mauricee.pontoon.login.login

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.Single
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.me.mauricee.pontoon.rule.MockkRule
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
import me.mauricee.pontoon.repository.session.LoginResult
import me.mauricee.pontoon.repository.session.SessionRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.login.LoginAction
import me.mauricee.pontoon.ui.login.LoginEvent
import me.mauricee.pontoon.ui.login.LoginPresenter
import me.mauricee.pontoon.ui.login.LoginState
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import java.net.HttpURLConnection

class LoginPresenterTests {

    @get:Rule
    val schedulerRule = SchedulerRule()

    @get:Rule
    val mockkRule = MockkRule(relaxed = true)

    @MockK
    lateinit var httpException: HttpException

    @MockK
    lateinit var mockAccountManagerHelper: SessionRepository

    @MockK
    lateinit var view: BaseContract.View<LoginAction>

    private lateinit var presenter: LoginPresenter

    @Before
    fun setUp() {
        presenter = LoginPresenter(mockAccountManagerHelper)
    }

    @Test
    fun shouldNavigateToMain_WhenActionLogin() {
        every { view.actions } returns LoginAction.Login("username", "password").toObservable()
        every { mockAccountManagerHelper.loginWithCredentials("username", "password") } returns Single.just(LoginResult.Success)
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
        events.assertValue(LoginEvent.NavigateToSession)
        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
    }

    @Test
    fun shouldRequest2Fa_WhenActionLogin() {
        every { view.actions } returns LoginAction.Login("username", "password").toObservable()
        every { mockAccountManagerHelper.loginWithCredentials("username", "password") } returns Single.just(LoginResult.Requires2FA)
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
                .assertValueAt(2, LoginState(prompt2FaCode = true, uiState = UiState.Success))
        events.assertEmpty()
        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
    }

    @Test
    fun shouldNavigateToMain_WhenAction2FA() {
        val authCode = "999999"
        every { view.actions } returns LoginAction.Authenticate(authCode).toObservable()
        every { mockAccountManagerHelper.authenticate(authCode) } returns Single.just(LoginResult.Success)

        presenter.attachView(view, LoginState())

        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
        events.assertValue(LoginEvent.NavigateToSession)
        verify(exactly = 1) { mockAccountManagerHelper.authenticate(authCode) }
    }

    @Test
    fun shouldError_MissingUsername_WhenActionLogin_MissingUsername() {
        every { view.actions } returns LoginAction.Login("", "password").toObservable()
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1) { it.uiState is UiState.Failed }
        events.assertEmpty()
        verify(exactly = 0) { mockAccountManagerHelper.loginWithCredentials(any(), any()) }
    }

    @Test
    fun shouldError_MissingPassword_WhenActionLogin_MissingPassword() {
        every { view.actions } returns LoginAction.Login("username", "").toObservable()
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1) { it.uiState is UiState.Failed }
        events.assertEmpty()
        verify(exactly = 0) { mockAccountManagerHelper.loginWithCredentials(any(), any()) }
    }

    @Test
    fun shouldError_Credentials_WhenActionLogin_HTTP_UNAUTHORIZED() {
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAUTHORIZED
        every { view.actions } returns LoginAction.Login("username", "password").toObservable()
        every { mockAccountManagerHelper.loginWithCredentials("username", "password") } returns Single.just(LoginResult.Error(httpException))
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
                .assertValueAt(2) { it.uiState is UiState.Failed }

        events.assertEmpty()
        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
    }

    @Test
    fun shouldError_Service_WhenActionLogin__HttpException_HTTP_UNAVAILABLE() {
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAVAILABLE
        every { view.actions } returns LoginAction.Login("username", "password").toObservable()
        every { mockAccountManagerHelper.loginWithCredentials("username", "password") } returns Single.just(LoginResult.Error(httpException))
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
                .assertValueAt(2) { it.uiState is UiState.Failed }

        events.assertEmpty()
        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
    }

    @Test
    fun shouldError_Network_WhenActionLogin_HttpExceptionOther() {
        every { httpException.code() } returns 600
        every { view.actions } returns LoginAction.Login("username", "password").toObservable()
        every { mockAccountManagerHelper.loginWithCredentials("username", "password") } returns Single.just(LoginResult.Error(httpException))
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
                .assertValueAt(2) { it.uiState is UiState.Failed }

        events.assertEmpty()
        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
    }

    @Test
    fun shouldError_General_WhenActionLogin() {
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAUTHORIZED
        every { view.actions } returns LoginAction.Login("username", "password").toObservable()
        every { mockAccountManagerHelper.loginWithCredentials("username", "password") } returns Single.just(LoginResult.Error(Exception()))
        val events = presenter.events.test()
        presenter.attachView(view, LoginState()).test()
                .assertValueAt(0, LoginState())
                .assertValueAt(1, LoginState(uiState = UiState.Loading))
                .assertValueAt(2) { it.uiState is UiState.Failed }

        events.assertEmpty()
        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
    }

//    @Test
//    fun shouldSuccessfullyLogin_WhenActionActivate() {
//        every { view.actions } returns LoginAction.LoginWithCookie("cookie:username").toObservable()
//        every { mockAccountManagerHelper.loginWithCookie("cookie", "username") } returns Single.just(LoginResult.Success)
//        val events = presenter.events.test()
//        presenter.attachView(view, LoginState()).test()
//                .assertValueAt(0, LoginState())
//                .assertValueAt(1, LoginState(uiState = UiState.Loading))
//        events.assertValue(LoginEvent.NavigateToSession)
//        verify(exactly = 1) { mockAccountManagerHelper.loginWithCredentials("username", "password") }
//    }
//
//    @Test
//    fun shouldError_Activation_WhenActionActivate_apiError_case1() {
//        every { view.actions } returns LoginAction.Activate("code", "username").toObservable()
//        every { floatPlaneApi.confirmEmail(any()) } returns Completable.error(Exception())
//        every { floatPlaneApi.self } returns user.toObservable()
//
//        presenter.attachView(view, LoginState())
//
//        verifyAll {
//            view getProperty "actions"
//            view.updateState(LoginContract.State.Loading)
//            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Activation))
//        }
//        verifyAll(inverse = true) {
//            mockAccountManagerHelper setProperty "account" value user
//            mockLoginNavigator.onSuccessfulLogin()
//        }
//    }
//
//    @Test
//    fun shouldError_Activation_WhenActionActivate_apiError_case2() {
//        every { view.actions } returns LoginAction.Activate("code", "username").toObservable()
//        every { floatPlaneApi.confirmEmail(any()) } returns Completable.complete()
//        every { floatPlaneApi.self } returns Observable.error(Exception())
//
//        presenter.attachView(view, LoginState())
//
//        verifyAll {
//            view getProperty "actions"
//            view.updateState(LoginContract.State.Loading)
//            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Activation))
//        }
//        verifyAll(inverse = true) {
//            mockAccountManagerHelper setProperty "account" value user
//            mockLoginNavigator.onSuccessfulLogin()
//        }
//    }
//
//    @Test
//    fun shouldNavigateToLttLogin_WhenActionLttLogin() {
//        every { view.actions } returns LoginAction.LttLogin.toObservable()
//        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()
//
//        presenter.attachView(view, LoginState())
//
//        verifyAll {
//            view getProperty "actions"
//            mockLoginNavigator.toLttLogin()
//        }
//    }
//
//    @Test
//    fun shouldNavigateToDiscordLogin_WhenActionDiscordLogin() {
//        every { view.actions } returns LoginAction.DiscordLogin.toObservable()
//        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()
//
//        presenter.attachView(view, LoginState())
//
//        verifyAll {
//            view getProperty "actions"
//            mockLoginNavigator.toDiscordLogin()
//        }
//    }
//
//    @Test
//    fun shouldNavigateToSignUp_WhenActionSignUp() {
//        every { view.actions } returns LoginAction.SignUp.toObservable()
//        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()
//
//        presenter.attachView(view, LoginState())
//
//        verifyAll {
//            view getProperty "actions"
//            mockLoginNavigator.toSignUp()
//        }
//    }
//
//    @Test
//    fun shouldSuccessfullyLoginWhen_Login() {
//        val cfuid = "id"
//        val sid = "sid"
//        val cookie = "${AuthInterceptor.CfDuid}=$cfuid;${AuthInterceptor.SailsSid}=$sid"
//        every { webLoginView.actions } returns WebLoginContract.Action.Login(cookie).toObservable()
//        every { floatPlaneApi.self } returns user.toObservable()
//
//        presenter.attachView(webLoginView)
//
//        verify {
//            accountManagerHelper.login("id", "sid")
//            accountManagerHelper setProperty "account" value user
//            loginNavigator.onSuccessfulLogin()
//        }
//    }
//
//    @Test
//    fun shouldDisplayError_WhenLogin_BadCookie() {
//        val cookie = ""
//        every { webLoginView.actions } returns WebLoginContract.Action.Login(cookie).toObservable()
//
//        presenter.attachView(webLoginView)
//
//        verify {
//            webLoginView.updateState(WebLoginContract.State.Error)
//        }
//
//        verify(inverse = true) {
//            accountManagerHelper.login(any(), any())
//            accountManagerHelper setProperty "account" value user
//            loginNavigator.onSuccessfulLogin()
//        }
//    }
}