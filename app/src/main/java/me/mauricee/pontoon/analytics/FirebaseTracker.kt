package me.mauricee.pontoon.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject


class FirebaseTracker @Inject constructor(private val analytics: FirebaseAnalytics) : EventTracker.Tracker {
    override fun trackAction(action: EventTracker.Action, page: EventTracker.Page) {
        Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT, page.name)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "action")
        }.also { analytics.logEvent(action.tag, it) }
    }

    override fun trackState(state: EventTracker.State, page: EventTracker.Page) {
        Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT, page.name)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "state")
        }.also { analytics.logEvent(state.tag, it) }
    }
}