package me.mauricee.pontoon.main.details

import android.support.v4.media.session.PlaybackStateCompat
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class DetailsPresenter @Inject constructor(private val player: Player,
                                           private val commentRepository: CommentRepository,
                                           private val videoRepository: VideoRepository,
                                           private val navigator: MainContract.Navigator) :
        DetailsContract.Presenter, BasePresenter<DetailsContract.State, DetailsContract.View>() {

    override fun onViewAttached(view: DetailsContract.View): Observable<DetailsContract.State> =
            view.actions.flatMap(this::handleAction).startWith(DetailsContract.State.Loading)
                    .mergeWith(Observable.merge(playerProgress(), playerState(), playerDuration()))

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

    private fun playerDuration(): Observable<DetailsContract.State> = player.duration.map {
        DetailsContract.State.Duration((it / 1000).toInt())
    }

    private fun playerProgress(): Observable<DetailsContract.State> = Observable.zip(
            player.progress().distinct(), player.bufferedProgress().distinct(), BiFunction { t1, t2 ->
        DetailsContract.State.Progress((t1 / 1000).toInt(), (t2 / 1000).toInt())
    })

    private fun playerState(): Observable<DetailsContract.State> = player.playbackState.map {
        when (it) {
            PlaybackStateCompat.STATE_CONNECTING,
            PlaybackStateCompat.STATE_BUFFERING -> DetailsContract.BufferState.Buffering
            else -> DetailsContract.BufferState.Playing
        }
    }.map(DetailsContract.State::PlaybackState)

    override fun onViewDetached() {
        super.onViewDetached()
        player.onPause()
    }

}