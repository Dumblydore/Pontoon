package me.mauricee.pontoon.main.details

import android.support.v4.media.session.PlaybackStateCompat
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class DetailsPresenter @Inject constructor(private val player: Player,
                                           private val userRepository: UserRepository,
                                           private val commentRepository: CommentRepository,
                                           private val videoRepository: VideoRepository,
                                           private val navigator: MainContract.Navigator,
                                           eventTracker: EventTracker) :
        DetailsContract.Presenter, BasePresenter<DetailsContract.State, DetailsContract.View>(eventTracker) {

    override fun onViewAttached(view: DetailsContract.View): Observable<DetailsContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(this::handleAction).startWith(DetailsContract.State.Loading)
                    .mergeWith(Observable.merge(playerProgress(), playerState(), playerDuration()))
                    .startWith(DetailsContract.State.CurrentUser(userRepository.activeUser))

    private fun handleAction(it: DetailsContract.Action): Observable<DetailsContract.State> = when (it) {
        is DetailsContract.Action.PlayVideo -> loadVideo(it)
        is DetailsContract.Action.Comment -> comment(it.text)
        is DetailsContract.Action.Reply -> comment(it.text)
        is DetailsContract.Action.SeekTo -> stateless { player.setProgress((it.position * 1000).toLong()) }
        is DetailsContract.Action.ViewUser -> stateless { navigator.toUser(it.user) }
        is DetailsContract.Action.ViewCreator -> stateless { navigator.toCreator(player.currentlyPlaying!!.video.creator) }
        is DetailsContract.Action.Like -> commentRepository.like(it.comment).map<DetailsContract.State> { DetailsContract.State.Like(it) }
                .onErrorReturnItem(DetailsContract.State.Error(DetailsContract.ErrorType.Like))
        is DetailsContract.Action.Dislike -> commentRepository.dislike(it.comment).map<DetailsContract.State> { DetailsContract.State.Dislike(it) }
                .onErrorReturnItem(DetailsContract.State.Error(DetailsContract.ErrorType.Dislike))
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

    private fun comment(text: String): Observable<DetailsContract.State> =
            commentRepository.comment(text, player.currentlyPlaying!!.video)
                    .map { DetailsContract.State.Comments(listOf(it)) }

    private fun reply(text: String, comment: Comment): Observable<DetailsContract.State> =
            commentRepository.comment(text, comment, player.currentlyPlaying!!.video)
                    .map { DetailsContract.State.Comments(listOf(it)) }

    override fun onViewDetached() {
        super.onViewDetached()
        player.onPause()
    }

}