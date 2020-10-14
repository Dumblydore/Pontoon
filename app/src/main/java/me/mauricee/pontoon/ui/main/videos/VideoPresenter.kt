package me.mauricee.pontoon.ui.main.videos

import io.reactivex.Observable
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.ShareManager
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.common.download.DownloadHelper
import me.mauricee.pontoon.ui.main.MainContract
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.rx.RxTuple
import retrofit2.HttpException
import javax.inject.Inject

class VideoPresenter @Inject constructor(private val subscriptionRepository: SubscriptionRepository,
                                         private val videoRepository: VideoRepository,
                                         private val mainNavigator: MainContract.Navigator,
                                         private val preferences: Preferences,
                                         private val sharedManager: ShareManager,
                                         private val downloadHelper: DownloadHelper,
                                         eventTracker: EventTracker) :

        BasePresenter<VideoContract.State, VideoContract.View>(eventTracker), VideoContract.Presenter {

    override fun onViewAttached(view: VideoContract.View): Observable<VideoContract.State> = view.actions
            .doOnNext { eventTracker.trackAction(it, view) }
            .flatMap(this::handleActions)
            .onErrorReturnItem(VideoContract.State.Error())

    private fun handleActions(action: VideoContract.Action): Observable<VideoContract.State> = when (action) {
        is VideoContract.Action.Refresh -> getVideos(action.clean).startWith(VideoContract.State.Loading())
        is VideoContract.Action.PlayVideo -> stateless { mainNavigator.playVideo(action.video) }
        is VideoContract.Action.Subscription -> stateless { action.creator.entity.apply { mainNavigator.toCreator(name, id) }  }
        is VideoContract.Action.Share -> stateless { sharedManager.shareVideo(action.video) }
        is VideoContract.Action.Download -> downloadVideo(action.video)
        VideoContract.Action.Creators -> stateless { mainNavigator.toCreatorsList() }
        VideoContract.Action.NavMenu -> stateless { mainNavigator.setMenuExpanded(true) }
    }

    private fun downloadVideo(video: Video): Observable<VideoContract.State> = Observable.empty()
//            = downloadHelper.download(video, qualityLevel)
//            .map { if (it) VideoContract.State.DownloadStart else VideoContract.State.DownloadFailed }
//            .onErrorReturnItem(VideoContract.State.DownloadFailed).toObservable()

    private fun getVideos(clean: Boolean) = RxTuple.combineLatestAsPair(preferences.displayUnwatchedVideos,
            subscriptionRepository.subscriptions.map { subs -> subs.map { it.id }.toTypedArray() }).map {
        val (unwatched, subscribedCreators) = it
        videoRepository.getVideos(unwatched, clean, *subscribedCreators)
    }.switchMap { feed ->
        Observable.merge(feed.videos.map(VideoContract.State::DisplayVideos),
                feed.state.map { processPaginationState(it, feed.retry) })
    }.onErrorReturn(::processError).mergeWith(subscriptionRepository.subscriptions.map(VideoContract.State::DisplaySubscriptions))

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