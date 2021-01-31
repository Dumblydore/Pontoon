package me.mauricee.pontoon.ui.main.videos

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.BaseViewModel
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

data class VideoState(val screenState: UiState = UiState.Empty,
                      val pageState: UiState = UiState.Empty,
                      val videos: PagedList<Video>? = null,
                      val subscriptions: List<Creator> = emptyList())

sealed class VideoReducer {
    object Loading : VideoReducer()
    object Fetching : VideoReducer()
    object Fetched : VideoReducer()
    data class FetchedVideos(val videos: PagedList<Video>) : VideoReducer()
    data class FetchedSubscriptions(val subscriptions: List<Creator>) : VideoReducer()
    data class ScreenError(val error: UiError) : VideoReducer()
    data class PageError(val error: UiError) : VideoReducer()
}

sealed class VideoAction : EventTracker.Action {
    object Refresh : VideoAction()
    class Subscription(val creator: Creator) : VideoAction()
    class PlayVideo(val video: Video) : VideoAction()
    class Share(val video: Video) : VideoAction()
    class Download(val video: Video) : VideoAction()
    object Creators : VideoAction()
    object NavMenu : VideoAction()
}

enum class VideoErrors(@StringRes val msg: Int) {
    Network(R.string.subscriptions_error_network),
    NoVideos(R.string.subscriptions_error_noVideos),
    Unknown(R.string.subscriptions_error_general)
}

typealias VideoEvent = Nothing

class VideoViewModel(p: VideoPresenter) : EventViewModel<VideoState, VideoAction, VideoEvent>(VideoState(), p) {
    class Factory @Inject constructor(p: VideoPresenter) : BaseViewModel.Factory<VideoViewModel>({ VideoViewModel(p) })
}