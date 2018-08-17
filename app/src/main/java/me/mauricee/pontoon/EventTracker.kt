package me.mauricee.pontoon

import me.mauricee.pontoon.ext.logd
import javax.inject.Inject

class EventTracker @Inject constructor(private val container: Page) {

    fun trackStart(page: Page) = trackAction(Start, page)

    fun trackStop(page: Page) = trackAction(Stop, page)

    fun trackAction(action: Action, page: Page) = logd("${container.trackerTag}:${page.trackerTag}:Action/${action.tag}")

    fun trackState(state: State, page: Page) = logd("${container.trackerTag}:${page.trackerTag}:State/${state.tag}")

    interface Page {
        val trackerTag: String
            get() = javaClass.simpleName.replace(regex, "")
    }

    interface State {
        val tag: String
            get() = javaClass.simpleName.replace(regex, "")
    }

    interface Action {
        val tag: String
            get() = javaClass.simpleName.replace(regex, "")
    }

    companion object {
        private object Start : Action {
            override val tag: String
                get() = "Start"
        }

        private object Stop : Action {
            override val tag: String
                get() = "End"
        }

        private val regex = Regex("Fragment|Activity")
    }
}