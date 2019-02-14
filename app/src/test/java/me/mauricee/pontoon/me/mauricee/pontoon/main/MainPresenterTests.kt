package me.mauricee.pontoon.me.mauricee.pontoon.main

import com.jakewharton.rxrelay2.PublishRelay
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.MainPresenter
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.me.mauricee.pontoon.rule.MockkRule
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainPresenterTests {

    @get:Rule
    val schedulerRule = SchedulerRule()
    @get:Rule
    val mockkRule = MockkRule(relaxed = true)


    @MockK
    lateinit var accountManagerHelper: AccountManagerHelper
    @MockK
    lateinit var animationTouchListener: VideoTouchHandler
    @MockK
    lateinit var userRepository: UserRepository
    @MockK
    lateinit var videoRepository: VideoRepository
    @MockK
    lateinit var player: Player
    @MockK
    lateinit var floatPlaneApi: FloatPlaneApi
    @MockK
    lateinit var pontoonDatabase: PontoonDatabase
    @MockK
    lateinit var authInterceptor: AuthInterceptor
    @MockK
    lateinit var navigator: MainContract.Navigator
    @MockK
    lateinit var view: MainContract.View
    @MockK
    lateinit var eventTracker: EventTracker
    @MockK
    lateinit var themeManager: ThemeManager

    private lateinit var presenter: MainPresenter

    @Before
    fun setUp() {
        presenter = MainPresenter(accountManagerHelper, animationTouchListener, userRepository, videoRepository, player, floatPlaneApi, pontoonDatabase, authInterceptor, themeManager, navigator, eventTracker)
    }

    @Test
    fun shouldLogout_whenLogout() {
        every { view.actions } returns MainContract.Action.SuccessfulLogout.toObservable()
        every { floatPlaneApi.logout() } returns Completable.complete()

        presenter.attachView(view)

        verify {
            accountManagerHelper.logout()
            pontoonDatabase.clearAllTables()
            view.updateState(MainContract.State.Logout)
        }
    }

    @Test
    fun shouldLogout_whenLogout_error_db() {
        every { view.actions } returns MainContract.Action.SuccessfulLogout.toObservable()
        every { floatPlaneApi.logout() } returns Completable.complete()
        every { pontoonDatabase.clearAllTables() } throws Throwable()

        presenter.attachView(view)

        verify {
            accountManagerHelper.logout()
            pontoonDatabase.clearAllTables()
            view.updateState(MainContract.State.Logout)
        }
    }

    @Test
    fun shouldLogout_whenLogout_network() {
        every { view.actions } returns MainContract.Action.SuccessfulLogout.toObservable()
        every { floatPlaneApi.logout() } returns Completable.error(Throwable())

        presenter.attachView(view)

        verify {
            accountManagerHelper.logout()
            pontoonDatabase.clearAllTables()
            view.updateState(MainContract.State.Logout)
        }
    }

    @Test
    fun shouldPreferences_whenPreferences() {
        every { view.actions } returns MainContract.Action.Preferences.toObservable()

        presenter.attachView(view)

        verify {
            navigator.toPreferences()
        }
    }

    @Test
    fun shouldSessionExpired_whenExpired() {
        val testObserver = PublishRelay.create<MainContract.Action>()
        every { authInterceptor.sessionExpired } returns Observable.just(true)
        every { view.actions } returns testObserver
        every { view.updateState(MainContract.State.SessionExpired) } answers {
            testObserver.accept(MainContract.Action.Expired)
        }

        presenter.attachView(view)

        verifyOrder {
            view.updateState(MainContract.State.SessionExpired)
            view.updateState(MainContract.State.Logout)
        }

        verify {
            accountManagerHelper.logout()
            pontoonDatabase.clearAllTables()
            view.updateState(MainContract.State.Logout)
        }
    }

    @Test
    fun shouldCurrentUser_whenProfile() {
        val user: UserRepository.User = mockk()
        every { view.actions } returns MainContract.Action.Profile.toObservable()
        every { userRepository.activeUser } returns user

        presenter.attachView(view)

        verify {
            navigator.toUser(user)
        }
    }


}