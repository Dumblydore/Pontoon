package me.mauricee.pontoon.main.history

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.video.Video

interface HistoryContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>
    sealed class Action {
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State {
        object Loading : State()
        class DisplayVideos(val videos: PagedList<Video>) : State()
        class Error(val type: Type = Type.Unknown) : State() {
            enum class Type(@StringRes val msg: Int) {
                NoVideos(R.string.history_error_noVideos),
                Unknown(R.string.history_error_general)
            }
        }
    }
}
