package me.mauricee.pontoon.main.videos

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.ShareManager
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.common.download.DownloadHelper
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.livestream.LiveStreamRepository
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import retrofit2.HttpException
import javax.inject.Inject

class VideoPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                         private val liveStreamRepository: LiveStreamRepository,
                                         private val mainNavigator: MainContract.Navigator,
                                         private val preferences: Preferences,
                                         private val sharedManager: ShareManager,
                                         private val downloadHelper: DownloadHelper,
                                         eventTracker: EventTracker) :

        BasePresenter<VideoContract.State, VideoContract.View>(eventTracker), VideoContract.Presenter {
private lateinit var sub: Disposable
    override fun onViewAttached(view: VideoContract.View): Observable<VideoContract.State> {
        sub = liveStreamRepository.activeLiveStreams.subscribe { it ->
            logd("List of active streams: ${it.size}")
        }
        return view.actions
                .doOnNext { eventTracker.trackAction(it, view) }
                .flatMap(this::handleActions)
                .onErrorReturnItem(VideoContract.State.Error())
    }

    private fun handleActions(action: VideoContract.Action): Observable<VideoContract.State> = when (action) {
        is VideoContract.Action.Refresh -> getVideos(action.clean).startWith(VideoContract.State.Loading())
        is VideoContract.Action.PlayVideo -> stateless { mainNavigator.playVideo(action.video) }
        is VideoContract.Action.Subscription -> stateless { mainNavigator.toCreator(action.creator) }
        is VideoContract.Action.Share -> stateless { sharedManager.shareVideo(action.video) }
        is VideoContract.Action.Download -> downloadVideo(action.video, action.quality)
        VideoContract.Action.Creators -> stateless { mainNavigator.toCreatorsList() }
        VideoContract.Action.NavMenu -> stateless { mainNavigator.setMenuExpanded(true) }
    }

    private fun downloadVideo(video: Video, qualityLevel: Player.QualityLevel) = downloadHelper.download(video, qualityLevel)
            .map { if (it) VideoContract.State.DownloadStart else VideoContract.State.DownloadFailed }
            .onErrorReturnItem(VideoContract.State.DownloadFailed).toObservable()

    private fun getVideos(clean: Boolean) = preferences.displayUnwatchedVideos
            .flatMap { videoRepository.getSubscriptionFeed(it, clean) }
            .flatMap<VideoContract.State> { feed ->
                Observable.merge(feed.videos.videos.map(VideoContract.State::DisplayVideos),
                        feed.videos.state.map { processPaginationState(it, feed.videos.retry) })
                        .startWith(VideoContract.State.DisplaySubscriptions(feed.subscriptions))
            }
            .onErrorReturn(::processError)

    private fun processError(e: Throwable): VideoContract.State.Error = when (e) {
        is VideoRepository.NoSubscriptionsException -> VideoContract.State.Error.Type.NoSubscriptions
        is HttpException -> VideoContract.State.Error.Type.Network
        else -> VideoContract.State.Error.Type.Unknown
    }.let(VideoContract.State::Error)

    private fun processPaginationState(state: StateBoundaryCallback.State, retry: () -> Unit): VideoContract.State = when (state) {
        StateBoundaryCallback.State.Loading -> VideoContract.State.Loading(false)
        StateBoundaryCallback.State.Error -> VideoContract.State.FetchError(VideoContract.State.FetchError.Type.Network, retry)
        StateBoundaryCallback.State.Fetched -> VideoContract.State.FinishPageFetch
        StateBoundaryCallback.State.Finished -> VideoContract.State.FetchError(VideoContract.State.FetchError.Type.NoVideos, retry)
    }
}