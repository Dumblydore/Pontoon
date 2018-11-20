package me.mauricee.pontoon.main.videos

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.VideoRepository
import retrofit2.HttpException
import javax.inject.Inject

class VideoPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                         private val mainNavigator: MainContract.Navigator,
                                         private val preferences: Preferences,
                                         eventTracker: EventTracker) :

        BasePresenter<VideoContract.State, VideoContract.View>(eventTracker), VideoContract.Presenter {

    override fun onViewAttached(view: VideoContract.View): Observable<VideoContract.State> = view.actions
            .doOnNext { eventTracker.trackAction(it, view) }
            .flatMap(this::handleActions)

    private fun handleActions(action: VideoContract.Action): Observable<VideoContract.State> = when (action) {
        is VideoContract.Action.Refresh -> getVideos(action.clean).startWith(VideoContract.State.Loading())
        is VideoContract.Action.PlayVideo -> stateless { mainNavigator.playVideo(action.video) }
        is VideoContract.Action.Subscription -> stateless { mainNavigator.toCreator(action.creator) }
        VideoContract.Action.Creators -> stateless { mainNavigator.toCreatorsList() }
    }

private fun getVideos(clean: Boolean) = preferences.displayUnwatchedVideos
            .flatMap { videoRepository.getSubscriptionFeed(it, clean) }
            .flatMap<VideoContract.State> { feed ->
                Observable.merge(feed.videos.videos.map(VideoContract.State::DisplayVideos),
                        feed.videos.state.map { processPaginationState(it, feed.videos.retry) })
                        .startWith(VideoContract.State.DisplaySubscriptions(feed.subscriptions))
            }.onErrorReturn(::processError)

    private fun processError(e: Throwable): VideoContract.State.Error = when (e) {
        is VideoRepository.NoSubscriptionsException -> VideoContract.State.Error.Type.NoSubscriptions
        is HttpException -> VideoContract.State.Error.Type.Network
        else -> VideoContract.State.Error.Type.Unknown
    }.let(VideoContract.State::Error)

    private fun processPaginationState(state: StateBoundaryCallback.State, retry: () -> Unit): VideoContract.State = when (state) {
        StateBoundaryCallback.State.LOADING -> VideoContract.State.Loading(false)
        StateBoundaryCallback.State.ERROR -> VideoContract.State.FetchError(VideoContract.State.FetchError.Type.Network, retry)
        StateBoundaryCallback.State.FETCHED -> VideoContract.State.FinishPageFetch
        StateBoundaryCallback.State.FINISHED -> VideoContract.State.FetchError(VideoContract.State.FetchError.Type.NoVideos, retry)
    }
}