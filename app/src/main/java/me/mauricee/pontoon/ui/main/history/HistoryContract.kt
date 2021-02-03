package me.mauricee.pontoon.ui.main.history

import androidx.annotation.StringRes
import androidx.paging.PagedList
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

interface HistoryContract {

    @HiltViewModel
    class ViewModel @Inject constructor(p: HistoryPresenter) : EventViewModel<State, Action, Nothing>(State(), p)

    data class State(val uiState: UiState = UiState.Empty,
                     val videos: PagedList<Video>? = null)

    sealed class Reducer {
        object Loading : Reducer()
        data class Fetched(val videos: PagedList<Video>) : Reducer()
        data class Error(val error: Errors) : Reducer()
    }

    sealed class Action : EventTracker.Action {
        class PlayVideo(val video: Video) : Action()
    }

    enum class Errors(@StringRes val msg: Int) {
        NoVideos(R.string.history_error_noVideos),
        Unknown(R.string.history_error_general)
    }
}
