package me.mauricee.pontoon.analytics

import com.crashlytics.android.Crashlytics
import java.util.*
import javax.inject.Inject

class CrashlyticsTracker @Inject constructor(): EventTracker.Tracker {
    private val actionStateQueue: Queue<String> = ArrayDeque()

    init {
        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            Crashlytics.log(actionStateQueue.reduce { i, i2 -> i + "$i2\n" })
        }
    }

    override fun trackAction(action: EventTracker.Action, page: EventTracker.Page) {
        if (action.level != EventTracker.Level.DEBUG)
            actionStateQueue.offer("$page:action=>$action")
    }

    override fun trackState(state: EventTracker.State, page: EventTracker.Page) {
        if (state.level != EventTracker.Level.DEBUG)
            actionStateQueue.offer("$page:state=>$state")
    }
}