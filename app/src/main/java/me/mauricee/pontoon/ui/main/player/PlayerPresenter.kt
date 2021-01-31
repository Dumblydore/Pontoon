package me.mauricee.pontoon.ui.main.player

import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.common.gestures.GestureEvent
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.rx.Either
import me.mauricee.pontoon.rx.RxTuple
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.StatefulPresenter
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: NewPlayer,
                                          private val videoRepo: VideoRepository,
                                          private val commentRepo: CommentRepository,
                                          private val animationTouchListener: VideoTouchHandler) : StatefulPresenter<PlayerState, PlayerAction>() {


    override fun onViewAttached(view: BaseContract.View<PlayerState, PlayerAction>): Observable<PlayerState> {
        return RxTuple.combineLatestAsPair(view.actions.flatMap(::onAction), gestures().distinctUntilChanged()).flatMap {
            val (actionState, newViewMode) = it
            when {
                state.viewMode != newViewMode -> setViewMode(actionState, newViewMode)
                else -> actionState.toObservable()
            }
        }
    }

    private fun gestures(): Observable<ViewMode> = animationTouchListener.events.map { gesture ->
        when (gesture) {
            is GestureEvent.Click -> handleClick(state.viewMode)
            is GestureEvent.Dismiss -> ViewMode.None(true)
            is GestureEvent.Scale -> ViewMode.Scale(gesture.percentage)
            is GestureEvent.Swipe -> ViewMode.Swipe(gesture.percentage)
            is GestureEvent.Expand -> if (gesture.isExpanded) ViewMode.Expanded(true) else ViewMode.PictureInPicture
        }
    }

    private fun onAction(action: PlayerAction): Observable<PlayerState> = when (action) {
        PlayerAction.ViewCreator -> stateless { }
        is PlayerAction.PlayVideo -> playVideo(action.videoId)
        is PlayerAction.Like -> stateless { }
        is PlayerAction.Reply -> stateless { }
        is PlayerAction.Dislike -> stateless { }
        is PlayerAction.ViewReplies -> stateless { }
        is PlayerAction.ViewUser -> stateless { }
        is PlayerAction.SetViewMode -> setViewMode(state, action.viewMode)
    }

    private fun playVideo(videoId: String): Observable<PlayerState> = videoRepo.addToWatchHistory(videoId).andThen(RxTuple.combineLatestAsQuad(
            player.playItem(videoId).andThen(Unit.toObservable()),
            videoRepo.getVideo(videoId).map { Either.either<Video, PlayerErrors>(it) }.onErrorReturnItem(Either.or(PlayerErrors.General)),
            videoRepo.getRelatedVideos(videoId).map { Either.either<List<Video>, PlayerErrors>(it) }.onErrorReturnItem(Either.or(PlayerErrors.NoRelatedVideos)),
            commentRepo.getComments(videoId).pages)).map {
        val (_, video, relatedVideos, comments) = it
        val errors = state.errors.toMutableList()
        video.or(errors::add)
        relatedVideos.or(errors::add)
        state.copy(isLoading = false, video = video.value, relatedVideos = relatedVideos.value
                ?: emptyList(), comments = comments, errors = errors)
    }.startWith(state.copy(isLoading = true, errors = emptyList()))

    private fun setViewMode(state: PlayerState, newViewMode: ViewMode): Observable<PlayerState> = Observable.defer {
        val oldViewMode = state.viewMode
        when (newViewMode) {
            is ViewMode.None -> player.stop()
            ViewMode.PictureInPicture -> Completable.complete()
            is ViewMode.FullScreen -> Completable.complete()
            is ViewMode.Expanded -> Completable.complete()
            is ViewMode.Scale -> Completable.complete()
            is ViewMode.Swipe -> Completable.complete()
        }.andThen(Observable.just(state.copy(viewMode = newViewMode)))

    }

    private fun handleClick(viewMode: ViewMode) = when (viewMode) {
        is ViewMode.Expanded -> ViewMode.Expanded(!viewMode.controlsEnabled)
        is ViewMode.FullScreen -> ViewMode.FullScreen(!viewMode.controlsEnabled)
        else -> viewMode
    }
}