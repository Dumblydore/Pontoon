package me.mauricee.pontoon.me.mauricee.pontoon.login.login

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.mockk.verifySequence
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.login.LoginNavigator
import me.mauricee.pontoon.login.login.LoginContract
import me.mauricee.pontoon.login.login.LoginPresenter
import org.junit.Before
import org.junit.Test

class LoginPresenterTests {
    @MockK
    lateinit var view: LoginContract.View
    @MockK
    lateinit var floatPlaneApi: FloatPlaneApi
    @MockK
    lateinit var mockAccountManagerHelper: AccountManagerHelper
    @MockK
    lateinit var mockLoginNavigator: LoginNavigator
    @MockK
    lateinit var mockEventTracker: EventTracker

    lateinit var presenter: LoginPresenter

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        presenter = LoginPresenter(floatPlaneApi, mockAccountManagerHelper, mockLoginNavigator, mockEventTracker)
    }

    @Test
    fun shouldNavigateToMain_WhenActionLogin() {
        every { view.actions } returns LoginContract.Action.Login("username", "password").toObservable()

        presenter.attachView(view)

        verifySequence {
            view.updateState(LoginContract.State.Loading)
            mockLoginNavigator.onSuccessfulLogin()
        }
    }

    @Test
    fun shouldError_MissingUsername_WhenActionLogin_MissingUsername() {

    }

    @Test
    fun shouldError_MissingPassword_WhenActionLogin_MissingPassword() {

    }

    @Test
    fun shouldError_Credentials_WhenActionLogin() {

    }

    @Test
    fun shouldError_Network_WhenActionLogin() {

    }

    @Test
    fun shouldError_Service_WhenActionLogin() {

    }

    @Test
    fun should_WhenActionActivate() {

    }

    @Test
    fun shouldNavigateToLttLogin_WhenActionLttLogin() {

    }

    @Test
    fun shouldNavigateToDiscordLogin_WhenActionDiscordLogin() {

    }

    @Test
    fun shouldNavigateToSignUp_WhenActionSignUp() {

    }
}