package me.mauricee.pontoon.me.mauricee.pontoon.login.login

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.verifyAll
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.LoginAuthToken
import me.mauricee.pontoon.domain.floatplane.LoginRequest
import me.mauricee.pontoon.domain.floatplane.UserJson
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import me.mauricee.pontoon.login.login.LoginContract
import me.mauricee.pontoon.login.login.LoginPresenter
import me.mauricee.pontoon.me.mauricee.pontoon.rule.MockkRule
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
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
    lateinit var floatPlaneApi: FloatPlaneApi
    @MockK
    lateinit var mockAccountManagerHelper: AccountManagerHelper
    @MockK
    lateinit var mockLoginNavigator: LoginNavigator
    @MockK
    lateinit var mockEventTracker: EventTracker
    @MockK
    lateinit var view: LoginContract.View
    @RelaxedMockK
    lateinit var user: UserJson
    @RelaxedMockK
    lateinit var userContainer: UserJson.Container

    private lateinit var presenter: LoginPresenter

    @Before
    fun setUp() {
        presenter = LoginPresenter(floatPlaneApi, mockAccountManagerHelper, mockLoginNavigator, mockEventTracker)
        every { userContainer.user } returns user
    }

    @Test
    fun shouldNavigateToMain_WhenActionLogin() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldRequest2Fa_WhenActionLogin() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()
        every { userContainer.needs2Fa } returns true

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Request2FaCode)
        }
        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldNavigateToMain_WhenAction2FA() {
        every { view.actions } returns LoginContract.Action.Authenticate("999999").toObservable()
        every { floatPlaneApi.login(any<LoginAuthToken>()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }


    @Test
    fun shouldError_MissingUsername_WhenActionLogin_MissingUsername() {
        every { view.actions } returns LoginContract.Action.Login("", "password").toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.MissingUsername))
        }
        verify(inverse = true) {
            floatPlaneApi.login(any<LoginRequest>())
            mockLoginNavigator.onSuccessfulLogin()
            view.updateState(LoginContract.State.Loading)
        }
    }

    @Test
    fun shouldError_MissingPassword_WhenActionLogin_MissingPassword() {
        every { view.actions } returns LoginContract.Action.Login("username", "").toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.MissingPassword))
        }
        verifyAll(inverse = true) {
            floatPlaneApi.login(any<LoginRequest>())
            mockLoginNavigator.onSuccessfulLogin()
            view.updateState(LoginContract.State.Loading)
        }
    }

    @Test
    fun shouldError_Credentials_WhenActionLogin_HTTP_UNAUTHORIZED() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAUTHORIZED
        every { floatPlaneApi.login(any<LoginRequest>()) } returns Observable.error(httpException)

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.NetworkError(LoginContract.State.NetworkError.Type.Credentials, HttpURLConnection.HTTP_UNAUTHORIZED))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_Service_WhenActionLogin__HttpException_HTTP_UNAVAILABLE() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns Observable.error(httpException)
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAVAILABLE

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.NetworkError(LoginContract.State.NetworkError.Type.Service, HttpURLConnection.HTTP_UNAVAILABLE))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_Network_WhenActionLogin_HttpExceptionOther() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns Observable.error(httpException)
        every { httpException.code() } returns 600

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.NetworkError(LoginContract.State.NetworkError.Type.Unknown, 600))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_General_WhenActionLogin() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns Observable.error(Exception())

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.General))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldSuccessfullyLogin_WhenActionActivate() {
        every { view.actions } returns LoginContract.Action.Activate("code", "username").toObservable()
        every { floatPlaneApi.confirmEmail(any()) } returns Completable.complete()
        every { floatPlaneApi.self } returns user.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_Activation_WhenActionActivate_apiError_case1() {
        every { view.actions } returns LoginContract.Action.Activate("code", "username").toObservable()
        every { floatPlaneApi.confirmEmail(any()) } returns Completable.error(Exception())
        every { floatPlaneApi.self } returns user.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Activation))
        }
        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_Activation_WhenActionActivate_apiError_case2() {
        every { view.actions } returns LoginContract.Action.Activate("code", "username").toObservable()
        every { floatPlaneApi.confirmEmail(any()) } returns Completable.complete()
        every { floatPlaneApi.self } returns Observable.error(Exception())

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Activation))
        }
        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldNavigateToLttLogin_WhenActionLttLogin() {
        every { view.actions } returns LoginContract.Action.LttLogin.toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            mockLoginNavigator.toLttLogin()
        }
    }

    @Test
    fun shouldNavigateToDiscordLogin_WhenActionDiscordLogin() {
        every { view.actions } returns LoginContract.Action.DiscordLogin.toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            mockLoginNavigator.toDiscordLogin()
        }
    }

    @Test
    fun shouldNavigateToSignUp_WhenActionSignUp() {
        every { view.actions } returns LoginContract.Action.SignUp.toObservable()
        every { floatPlaneApi.login(any<LoginRequest>()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            mockLoginNavigator.toSignUp()
        }
    }
}