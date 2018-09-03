package me.mauricee.pontoon.main.videos

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.video.VideoRepository
import retrofit2.HttpException
import javax.inject.Inject

class VideoPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                         private val mainNavigator: MainContract.Navigator,
                                         eventTracker: EventTracker) :

        BasePresenter<VideoContract.State, VideoContract.View>(eventTracker), VideoContract.Presenter {

    override fun onViewAttached(view: VideoContract.View): Observable<VideoContract.State> = view.actions
            .doOnNext { eventTracker.trackAction(it, view) }
            .flatMap(this::handleActions)

    private fun handleActions(action: VideoContract.Action): Observable<VideoContract.State> = when (action) {
        is VideoContract.Action.Refresh -> getVideos()
        is VideoContract.Action.PlayVideo -> stateless { mainNavigator.playVideo(action.video) }
        is VideoContract.Action.Subscription -> stateless { mainNavigator.toCreator(action.creator) }
        VideoContract.Action.Creators -> stateless { mainNavigator.toCreatorsList() }
    }

    private fun getVideos() = videoRepository.getSubscriptionFeed()
            .flatMap<VideoContract.State> {
                Observable.just(VideoContract.State.DisplayVideos(it.videos),
                        VideoContract.State.DisplaySubscriptions(it.subscriptions))
            }.onErrorReturn(::processError)

    private fun processError(e: Throwable): VideoContract.State.Error = when (e) {
        is VideoRepository.NoSubscriptionsException -> VideoContract.State.Error.Type.NoVideos
        is HttpException -> VideoContract.State.Error.Type.Network
        else -> VideoContract.State.Error.Type.Unknown
    }.let(VideoContract.State::Error)
}