package me.mauricee.pontoon.tv.detail

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.ui.BaseViewModel
import me.mauricee.pontoon.ui.UiState

data class DetailState(
        val uiState: UiState = UiState.Empty,
        val video: Video? = null,
        val relatedVideos: List<Video> = emptyList()
)

sealed class DetailReducer {
    object Loading : DetailReducer()
    data class UpdateVideo(val video: Video) : DetailReducer()
    data class UpdateRelatedVideos(val videos: List<Video>) : DetailReducer()
}

typealias DetailAction = Unit
typealias DetailEvent = Unit

class DetailViewModel @AssistedInject constructor(@Assisted p: DetailPresenter) : BaseViewModel<DetailState, DetailAction, DetailEvent>(DetailState(), p) {
    @AssistedFactory
    interface Factory {
        fun create(p: DetailPresenter): DetailViewModel
    }
}