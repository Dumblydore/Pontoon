package me.mauricee.pontoon.main.details

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class DetailsPresenter @Inject constructor(private val player: Player,
                                           private val commentRepository: CommentRepository,
                                           private val videoRepository: VideoRepository,
                                           private val navigator: MainContract.Navigator,
                                           eventTracker: EventTracker) :
        DetailsContract.Presenter, BasePresenter<DetailsContract.State, DetailsContract.View>(eventTracker) {

    override fun onViewAttached(view: DetailsContract.View): Observable<DetailsContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(this::handleAction).startWith(DetailsContract.State.Loading)

    private fun handleAction(it: DetailsContract.Action): Observable<DetailsContract.State> = when (it) {
        is DetailsContract.Action.PlayVideo -> loadVideo(it)
        is DetailsContract.Action.Comment -> TODO()
        is DetailsContract.Action.SeekTo -> stateless { player.setProgress((it.position * 1000).toLong()) }
        is DetailsContract.Action.ViewUser -> stateless { navigator.toUser(it.user) }
        is DetailsContract.Action.ViewCreator -> stateless { navigator.toCreator(it.creator) }
    }.onErrorReturnItem(DetailsContract.State.Error())

    private fun loadVideo(video: DetailsContract.Action.PlayVideo): Observable<DetailsContract.State> = video.id.let { id ->
        Observable.merge(loadRelatedVideos(id), loadComments(id), loadVideoDetails(video))
    }

    private fun loadVideoDetails(action: DetailsContract.Action.PlayVideo): Observable<DetailsContract.State> =
            videoRepository.getVideo(action.id).doOnSuccess(videoRepository::addToWatchHistory).flatMapObservable { video ->
                videoRepository.getQualityOfVideo(action.id).doOnNext { player.currentlyPlaying = Playback(video, it) }
                        .map { DetailsContract.State.VideoInfo(video) }
            }

    private fun loadRelatedVideos(video: String): Observable<DetailsContract.State> =
            videoRepository.getRelatedVideos(video).toObservable()
                    .map(DetailsContract.State::RelatedVideos)

    private fun loadComments(video: String): Observable<DetailsContract.State> =
            commentRepository.getComments(video)
                    .flatMapSingle { it.toObservable().filter { it.video == it.parent }.toList() }
                    .onErrorReturnItem(emptyList())
                    .map(DetailsContract.State::Comments)


    override fun onViewDetached() {
        super.onViewDetached()
        player.onPause()
    }

}