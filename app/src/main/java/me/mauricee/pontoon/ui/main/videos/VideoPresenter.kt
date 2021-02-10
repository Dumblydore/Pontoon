package me.mauricee.pontoon.ui.main.videos

import com.jakewharton.rx.replayingShare
import io.reactivex.Observable
import me.mauricee.pontoon.common.PagingState
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

class VideoPresenter @Inject constructor(private val subscriptionRepository: SubscriptionRepository,
                                         private val videoRepository: VideoRepository,
                                         private val preferences: Preferences) : BasePresenter<VideoState, VideoReducer, VideoAction, VideoEvent>() {

    override fun onViewAttached(view: BaseContract.View<VideoAction>): Observable<VideoReducer> {
        return preferences.displayUnwatchedVideos.switchMap { displayUnwatchedVideos ->
            val actions = view.actions.replayingShare()
            val subscriptions = subscriptionRepository.subscriptions
            val refreshes = actions.filter { it is VideoAction.Refresh }
                    .map { true }
                    .startWith(false)
                    .switchMap { fresh ->
                        if (fresh) subscriptions.fetch().toObservable()
                        else subscriptions.get()
                    }.switchMap { load(displayUnwatchedVideos, it) }.onErrorReturnItem(VideoReducer.ScreenError(UiError(VideoErrors.Network.msg)))
            val otherActions = actions.filter { it !is VideoAction.Refresh }
                    .flatMap { handleAction(it) }
            Observable.merge(refreshes, otherActions)
        }
    }

    override fun onReduce(state: VideoState, reducer: VideoReducer): VideoState = when (reducer) {
        VideoReducer.Loading -> state.copy(screenState = UiState.Loading, pageState = UiState.Empty)
        VideoReducer.Fetching -> state.copy(pageState = UiState.Loading)
        VideoReducer.Fetched -> state.copy(screenState = UiState.Success, pageState = UiState.Success)
        is VideoReducer.FetchedVideos -> state.copy(videos = reducer.videos)
        is VideoReducer.FetchedSubscriptions -> state.copy(subscriptions = reducer.subscriptions)
        is VideoReducer.ScreenError -> state.copy(screenState = UiState.Failed(reducer.error))
        is VideoReducer.PageError -> state.copy(pageState = UiState.Failed(reducer.error))
    }


    private fun load(displayUnwatchedVideos: Boolean, subs: List<Creator>): Observable<VideoReducer> {
        val (pages, states) = videoRepository.getVideos(displayUnwatchedVideos, *subs.map(Creator::id).toTypedArray())
        return Observable.merge(pages.map(VideoReducer::FetchedVideos), states.map(::mapPagingStates))
                .startWith(VideoReducer.FetchedSubscriptions(subs))
    }

    private fun handleAction(action: VideoAction): Observable<VideoReducer> {
        return when (action) {
            is VideoAction.Subscription -> noReduce { sendEvent(VideoEvent.NavigateToCreator(action.creator.id)) }
            is VideoAction.Download -> noReduce { }
            VideoAction.Creators -> noReduce { sendEvent(VideoEvent.NavigateToAllCreators) }
            VideoAction.Refresh -> throw RuntimeException("Should not enter branch! (VideoAction.Refresh)")
        }
    }

    private fun mapPagingStates(state: PagingState): VideoReducer {
        return when (state) {
            PagingState.InitialFetch -> VideoReducer.Fetching
            PagingState.Fetching -> VideoReducer.Fetching
            PagingState.Fetched -> VideoReducer.Fetched
            PagingState.Completed -> VideoReducer.Fetched
            PagingState.Empty -> VideoReducer.PageError(UiError(VideoErrors.NoVideos.msg))
            PagingState.Error -> VideoReducer.PageError(UiError(VideoErrors.Unknown.msg))
        }
    }
}
