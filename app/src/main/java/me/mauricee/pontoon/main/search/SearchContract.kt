package me.mauricee.pontoon.main.search

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.video.Video

interface SearchContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        class Query(val query: String) : Action()
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        object FetchingPage : State()
        object FinishFetching : State()
        class FetchError(val type: Type = Type.General, val retry: () -> Unit) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR
            override val tag: String
                get() = "${super.tag}_$type"
        }
        class Error(val type: Type = Type.General) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR
            override val tag: String
                get() = "${super.tag}_$type"
        }

        class Results(val list: PagedList<Video>) : State()

        enum class Type(@StringRes val msg: Int) {
            NoText(R.string.search_error_noText),
            NoResults(R.string.search_error_noResults),
            Network(R.string.search_error_network),
            General(R.string.search_error_general)
        }
    }
}