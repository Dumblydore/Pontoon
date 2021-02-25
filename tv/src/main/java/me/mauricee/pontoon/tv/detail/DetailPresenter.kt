package me.mauricee.pontoon.tv.detail

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Flowable
import io.reactivex.Observable
import me.mauricee.pontoon.repository.video.VideoRepository
import me.mauricee.pontoon.ui.ActionPresenter
import me.mauricee.pontoon.ui.UiState

class DetailPresenter @AssistedInject constructor(@Assisted private val videoId: String,
                                                  private val videoRepository: VideoRepository) : ActionPresenter<DetailState, DetailReducer, DetailAction, DetailEvent>() {

    override fun onViewAttached(): Observable<DetailReducer> {
        return Flowable.merge(videoRepository.getVideo(videoId).map(DetailReducer::UpdateVideo),
                videoRepository.getRelatedVideos(videoId).map(DetailReducer::UpdateRelatedVideos))
                .startWith(DetailReducer.Loading).toObservable()
    }

    override fun onReduce(state: DetailState, reducer: DetailReducer): DetailState {
        return when (reducer) {
            DetailReducer.Loading -> state.copy(uiState = UiState.Loading)
            is DetailReducer.UpdateRelatedVideos -> state.copy(relatedVideos = reducer.videos)
            is DetailReducer.UpdateVideo -> state.copy(uiState = UiState.Success, video = reducer.video)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(videoId: String): DetailPresenter
    }
}