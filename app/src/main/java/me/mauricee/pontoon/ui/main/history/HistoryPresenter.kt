package me.mauricee.pontoon.ui.main.history

import androidx.paging.PagedList
import io.reactivex.Observable
import io.reactivex.ObservableSource
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.repository.video.VideoRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

class HistoryPresenter @Inject constructor(private val videoRepository: VideoRepository) : BasePresenter<HistoryContract.State, HistoryContract.Reducer, HistoryContract.Action, Nothing>() {


    override fun onViewAttached(view: BaseContract.View<HistoryContract.Action>): Observable<HistoryContract.Reducer> {
        return videoRepository.watchHistory()
                .map(::checkForEmptyList)
                .onErrorReturnItem(HistoryContract.Reducer.Error(HistoryContract.Errors.Unknown))
                .toObservable()
                .mergeWith(view.actions.flatMap(::handleAction))
    }

    override fun onReduce(state: HistoryContract.State, reducer: HistoryContract.Reducer): HistoryContract.State {
        return when (reducer) {
            HistoryContract.Reducer.Loading -> state.copy(uiState = UiState.Loading)
            is HistoryContract.Reducer.Fetched -> state.copy(uiState = UiState.Success, videos = reducer.videos)
            is HistoryContract.Reducer.Error -> state.copy(uiState = UiState.Failed(UiError(reducer.error.msg)))
        }
    }

    private fun handleAction(action: HistoryContract.Action): ObservableSource<HistoryContract.Reducer> {
        return when (action) {
            is HistoryContract.Action.PlayVideo -> noReduce { /*navigator.playVideo(action.video.id) */}
        }
    }

    private fun checkForEmptyList(it: PagedList<Video>) = if (it.isEmpty())
        HistoryContract.Reducer.Error(HistoryContract.Errors.NoVideos)
    else
        HistoryContract.Reducer.Fetched(it)
}