package me.mauricee.pontoon.me.mauricee.pontoon

import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.me.mauricee.pontoon.rule.MockkRule
import me.mauricee.pontoon.me.mauricee.pontoon.rule.SchedulerRule
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BasePresenterTests {
    @get:Rule
    val schedulerRule = SchedulerRule()

    @get:Rule
    val mockKRule = MockkRule(relaxed = true)

    @MockK
    lateinit var mockView: BaseContract.View<EventTracker.Action>
    private lateinit var basePresenter: BasePresenter<EventTracker.State, Unit, EventTracker.Action, Unit>

    @Before
    fun setUp() {
        basePresenter = spyk(Presenter())
    }
//
//    @Test
//    fun shouldAttachToView() {
//        basePresenter.attachView(mockView)
//
//        verify { basePresenter.attachView(mockView) }
//    }

    private class Presenter : BasePresenter<EventTracker.State, Unit, EventTracker.Action, Unit>() {
        override fun onReduce(state: EventTracker.State, reducer: Unit): EventTracker.State {
            return state
        }
    }
}