package me.mauricee.pontoon.analytics

import me.mauricee.pontoon.ext.logd
import javax.inject.Inject

class DebugTracker @Inject constructor() : EventTracker.Tracker {

    override fun trackAction(action: EventTracker.Action, page: EventTracker.Page) = logd("${page.name}:Action/${action.tag}")

    override fun trackState(state: EventTracker.State, page: EventTracker.Page) = logd("${page.name}:State/${state.tag}")

}