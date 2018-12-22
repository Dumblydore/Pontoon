package me.mauricee.pontoon.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import me.mauricee.pontoon.ext.just
import javax.inject.Inject


class FirebaseTracker @Inject constructor(private val analytics: FirebaseAnalytics) : EventTracker.Tracker {
    override fun trackAction(action: EventTracker.Action, page: EventTracker.Page) {
        if (action.level != EventTracker.Level.DEBUG) {
            Bundle().apply {
                putString(FirebaseAnalytics.Param.CONTENT, page.name)
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, "action")
            }.just { analytics.logEvent(action.tag, this) }
        }
    }

    override fun trackState(state: EventTracker.State, page: EventTracker.Page) {
        if (state.level != EventTracker.Level.DEBUG) {
            Bundle().apply {
                putString(FirebaseAnalytics.Param.CONTENT, page.name)
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, "state")
            }.just { analytics.logEvent(state.tag, this) }
        }
    }
}