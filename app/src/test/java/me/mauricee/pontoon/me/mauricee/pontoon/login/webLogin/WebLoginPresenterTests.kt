package me.mauricee.pontoon.me.mauricee.pontoon.login.webLogin

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.UserJson
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import me.mauricee.pontoon.login.webLogin.WebLoginContract
import me.mauricee.pontoon.login.webLogin.WebLoginPresenter
import me.mauricee.pontoon.me.mauricee.pontoon.rule.MockkRule
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WebLoginPresenterTests {

    @get:Rule
    val schedulerRule = SchedulerRule()
    @get:Rule
    val mockkRule = MockkRule(relaxed = true)

    @MockK
    lateinit var floatPlaneApi: FloatPlaneApi
    @MockK
    lateinit var loginNavigator: LoginNavigator
    @RelaxedMockK
    lateinit var webLoginView: WebLoginContract.View
    @RelaxedMockK
    lateinit var accountManagerHelper: AccountManagerHelper
    @RelaxedMockK
    lateinit var eventTracker: EventTracker
    @RelaxedMockK
    lateinit var user: UserJson

    private lateinit var presenter: WebLoginPresenter

    @Before
    fun setUp() {
        presenter = WebLoginPresenter(floatPlaneApi, accountManagerHelper, loginNavigator, eventTracker)
    }

    @Test
    fun shouldSuccessfullyLoginWhen_Login() {
        val cfuid = "id"
        val sid = "sid"
        val cookie = "${AuthInterceptor.CfDuid}=$cfuid;${AuthInterceptor.SailsSid}=$sid"
        every { webLoginView.actions } returns WebLoginContract.Action.Login(cookie).toObservable()
        every { floatPlaneApi.self } returns user.toObservable()

        presenter.attachView(webLoginView)

        verify {
            accountManagerHelper.login("id", "sid")
            accountManagerHelper setProperty "account" value user
            loginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldDisplayError_WhenLogin_BadCookie() {
        val cookie = ""
        every { webLoginView.actions } returns WebLoginContract.Action.Login(cookie).toObservable()

        presenter.attachView(webLoginView)

        verify {
            webLoginView.updateState(WebLoginContract.State.Error)
        }

        verify(inverse = true){
            accountManagerHelper.login(any(), any())
            accountManagerHelper setProperty "account" value user
            loginNavigator.onSuccessfulLogin()
        }
    }
}