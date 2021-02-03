package me.mauricee.pontoon.ui.main.creator

import androidx.annotation.StringRes
import androidx.paging.PagedList
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.BaseViewModel
import me.mauricee.pontoon.ui.UiState

interface CreatorContract {

    data class Args(val creator: String)

    sealed class Action : EventTracker.Action {
        object Refresh : Action()
        class PlayVideo(val video: Video) : Action()
    }

    sealed class Reducer {
        object Loading : Reducer()
        object Fetching : Reducer()
        object Fetched : Reducer()
        data class PageError(val error: Errors?) : Reducer()
        data class Error(val error: Errors?) : Reducer()
        data class DisplayCreator(val creator: Creator) : Reducer()
        data class DisplayVideos(val videos: PagedList<Video>) : Reducer()
    }

    sealed class Event


    class ViewModel @AssistedInject constructor(@Assisted p: CreatorPresenter) : BaseViewModel<State, Action>(State(), p) {
        @AssistedFactory
        interface Factory {
            fun create(p: CreatorPresenter): ViewModel
        }
    }

    enum class Errors(@StringRes val msg: Int) {
        Network(R.string.creator_error_noCreator),
        NoVideos(R.string.creator_error_noVideos),
        Unknown(R.string.creator_error_general)
    }

    data class State(val screenState: UiState = UiState.Empty,
                     val pageState: UiState = UiState.Empty,
                     val creator: Creator? = null,
                     val videos: PagedList<Video>? = null,
                     val error: Errors? = null)
}