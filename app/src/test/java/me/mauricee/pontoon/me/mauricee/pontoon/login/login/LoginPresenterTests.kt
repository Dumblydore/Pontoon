package me.mauricee.pontoon.me.mauricee.pontoon.login.login

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.User
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import me.mauricee.pontoon.login.login.LoginContract
import me.mauricee.pontoon.login.login.LoginPresenter
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_BAD_GATEWAY

class LoginPresenterTests {

    @get:Rule
    val schedulerRule = SchedulerRule()

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
    lateinit var user: User

    private lateinit var userContainer: User.Container
    private lateinit var presenter: LoginPresenter

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        presenter = LoginPresenter(floatPlaneApi, mockAccountManagerHelper, mockLoginNavigator, mockEventTracker)
        userContainer = User.Container("", user)
    }

    @Test
    fun shouldNavigateToMain_WhenActionLogin() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any()) } returns userContainer.toObservable()

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
            floatPlaneApi.login(any())
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
            floatPlaneApi.login(any())
            mockLoginNavigator.onSuccessfulLogin()
            view.updateState(LoginContract.State.Loading)
        }
    }

    @Test
    fun shouldError_Credentials_WhenActionLogin_HTTP_UNAUTHORIZED() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAUTHORIZED
        every { floatPlaneApi.login(any()) } returns Observable.error(httpException)

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Credentials))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_Service_WhenActionLogin__HttpException_HTTP_UNAVAILABLE() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any()) } returns Observable.error(httpException)
        every { httpException.code() } returns HttpURLConnection.HTTP_UNAVAILABLE

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Service))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_Network_WhenActionLogin_HttpExceptionOther() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any()) } returns Observable.error(httpException)
        every { httpException.code() } returns HTTP_BAD_GATEWAY

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            view.updateState(LoginContract.State.Loading)
            view.updateState(LoginContract.State.Error(LoginContract.State.Error.Type.Network))
        }

        verifyAll(inverse = true) {
            mockAccountManagerHelper setProperty "account" value user
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_General_WhenActionLogin() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()
        every { floatPlaneApi.login(any()) } returns Observable.error(Exception())

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
        every { floatPlaneApi.login(any()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            mockLoginNavigator.toLttLogin()
        }
    }

    @Test
    fun shouldNavigateToDiscordLogin_WhenActionDiscordLogin() {
        every { view.actions } returns LoginContract.Action.DiscordLogin.toObservable()
        every { floatPlaneApi.login(any()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            mockLoginNavigator.toDiscordLogin()
        }
    }

    @Test
    fun shouldNavigateToSignUp_WhenActionSignUp() {
        every { view.actions } returns LoginContract.Action.SignUp.toObservable()
        every { floatPlaneApi.login(any()) } returns userContainer.toObservable()

        presenter.attachView(view)

        verifyAll {
            view getProperty "actions"
            mockLoginNavigator.toSignUp()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}