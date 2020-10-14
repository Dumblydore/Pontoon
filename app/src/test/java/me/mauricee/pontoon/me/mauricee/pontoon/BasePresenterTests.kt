package me.mauricee.pontoon.me.mauricee.pontoon

import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.me.mauricee.pontoon.rule.MockkRule
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BasePresenterTests {
    @get:Rule
    val schedulerRule = SchedulerRule()
    @get:Rule
    val mockKRule = MockkRule(relaxed = true)

    @MockK
    lateinit var mockEventTracker: EventTracker
    @MockK
    lateinit var mockView: BaseContract.View<Any, Any>
    private lateinit var basePresenter: BasePresenter<EventTracker.State, BaseContract.View<Any, Any>>

    @Before
    fun setUp() {
        basePresenter = spyk(Presenter(mockEventTracker))
    }

    @Test
    fun shouldAttachToView() {
        basePresenter.attachView(mockView)

        verify { basePresenter.attachView(mockView) }
    }

    private class Presenter(eventTracker: EventTracker) : BasePresenter<EventTracker.State, BaseContract.View<Any, Any>>(eventTracker)
}