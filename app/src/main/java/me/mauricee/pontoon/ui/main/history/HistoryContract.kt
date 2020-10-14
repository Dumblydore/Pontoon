package me.mauricee.pontoon.ui.main.history

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.video.Video

interface HistoryContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>
    sealed class Action : EventTracker.Action {
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class DisplayVideos(val videos: PagedList<Video>) : State()
        class Error(val type: Type = Type.Unknown) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR
            enum class Type(@StringRes val msg: Int) {
                NoVideos(R.string.history_error_noVideos),
                Unknown(R.string.history_error_general)
            }
        }
    }
}
