package me.mauricee.pontoon.analytics

import javax.inject.Inject

class EventTracker @Inject constructor(private val container: Page) {

    fun trackStart(page: Page) = trackers.forEach { it.trackState(Start, page) }

    fun trackStop(page: Page) = trackers.forEach { it.trackState(Stop, page) }

    fun trackAction(action: Action, page: Page) = trackers.forEach { it.trackAction(action, page) }

    fun trackState(state: State, page: Page) = trackers.forEach { it.trackState(state, page) }

    interface Page {
        val name: String
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

        val trackers: MutableList<Tracker> = mutableListOf()

        private object Start : State {
            override val tag: String
                get() = "Start"
        }

        private object Stop : State {
            override val tag: String
                get() = "End"
        }

        private val regex = Regex("Fragment|Activity")
    }

    interface Tracker {
        fun trackAction(action: Action, page: Page)
        fun trackState(state: State, page: Page)
    }
}