package me.mauricee.pontoon

import me.mauricee.pontoon.ext.logd
import javax.inject.Inject

class EventTracker @Inject constructor(private val container: Page) {

    fun trackStart(page: Page) = logd("${container.name}:${page.name}/${Start.tag}")

    fun trackStop(page: Page) = logd("${container.name}:${page.name}/${Stop.tag}")

    fun trackAction(action: Action, page: Page) = logd("${container.name}:${page.name}:Action/${action.tag}")

    fun trackState(state: State, page: Page) = logd("${container.name}:${page.name}:State/${state.tag}")

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
}