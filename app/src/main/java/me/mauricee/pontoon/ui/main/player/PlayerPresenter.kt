package me.mauricee.pontoon.ui.main.player

import com.jakewharton.rx.replayingShare
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.common.PagingState
import me.mauricee.pontoon.ext.toDuration
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

class PlayerPresenter @Inject constructor(private val player: Player,
                                          private val userRepo: UserRepository,
                                          private val videoRepo: VideoRepository,
                                          private val commentRepo: CommentRepository) : BasePresenter<PlayerState, PlayerReducer, PlayerAction, PlayerEvent>() {

    override fun onViewAttached(view: BaseContract.View<PlayerAction>): Observable<PlayerReducer> {
        val actions = view.actions.replayingShare()
        val playVideo = actions.filter { it is PlayerAction.PlayVideo }
                .cast(PlayerAction.PlayVideo::class.java)
                .concatMapSingle { player.playItem(it.videoId).andThen(Single.just(it.videoId)) }
                .switchMap(videoRepo::getVideo)
                .switchMap { video ->
                    val otherActions = view.actions.filter { it !is PlayerAction.PlayVideo }
                            .flatMap { handleOtherActions(video, it) }
                    Observable.merge(loadVideoContents(video), otherActions)
                            .startWith(PlayerReducer.DisplayVideo(video))
                            .onErrorReturnItem(PlayerReducer.DisplayVideoError(UiError(PlayerErrors.General.message)))
                }.startWith(PlayerReducer.Loading)

        return Observable.merge(loadUser(), playVideo)
    }

    override fun onReduce(state: PlayerState, reducer: PlayerReducer): PlayerState = when (reducer) {
        PlayerReducer.Loading -> state.copy(videoState = UiState.Loading, relatedVideosState = UiState.Loading)
        is PlayerReducer.DisplayQualityLevels -> state.copy(qualityLevels = reducer.qualityLevels)
        is PlayerReducer.DisplayComments -> state.copy(commentState = UiState.Success, comments = reducer.comments)
        is PlayerReducer.DisplayVideo -> state.copy(viewMode = ViewMode.Expanded, videoState = UiState.Success, video = reducer.video)
        is PlayerReducer.DisplayRelatedVideo -> state.copy(relatedVideosState = UiState.Success, relatedVideos = reducer.videos)
        is PlayerReducer.UpdateViewMode -> state.copy(viewMode = reducer.viewMode)
        is PlayerReducer.DisplayVideoError -> state.copy(videoState = UiState.Failed(reducer.error))
        is PlayerReducer.DisplayRelatedVideosError -> state.copy(relatedVideosState = UiState.Failed(reducer.error))
        is PlayerReducer.DisplayCommentError -> state.copy(commentState = UiState.Failed(reducer.error))
        PlayerReducer.FetchingComments -> state.copy(commentState = UiState.Loading)
        PlayerReducer.CommentsFetched -> state.copy(commentState = UiState.Success)
        PlayerReducer.ToggleFullScreen -> state.copy(viewMode = handleClick(state.viewMode))
        is PlayerReducer.DisplayUser -> state.copy(user = reducer.user)
        is PlayerReducer.SetViewMode -> handleSetViewMode(state, reducer.viewMode)
        is PlayerReducer.DisplayTimelinePreview -> state.copy(timelineUrl = reducer.previewUrl)
        is PlayerReducer.DisplayCurrentQualityLevel -> state.copy(currentQualityLevel = reducer.qualityLevel)
        is PlayerReducer.UpdatePlayingState -> state.copy(isPlaying = reducer.isPlaying)
        is PlayerReducer.UpdatePosition -> state.copy(position = reducer.position, timestamp = "${reducer.position.toDuration()}:${state.duration?.toDuration()}")
        is PlayerReducer.UpdateDuration -> state.copy(duration = reducer.duration, timestamp = "${state.position?.toDuration()}:${reducer.duration.toDuration()}")
    }


    private fun handleSetViewMode(state: PlayerState, newViewMode: ViewMode): PlayerState {
        return when {
            state.viewMode == newViewMode -> state
            //  To prevent going from pip into fullscreen
            state.viewMode == ViewMode.PictureInPicture && newViewMode == ViewMode.Fullscreen -> state
            else -> state.copy(viewMode = newViewMode)
        }
    }

    private fun handleOtherActions(video: Video, action: PlayerAction): Observable<PlayerReducer> = when (action) {
        PlayerAction.ViewCreator -> noReduce { /*mainNavigator.toCreator(video.creator.id) */ }
        is PlayerAction.Like -> noReduce(commentRepo.like(action.comment.id))
        is PlayerAction.Dislike -> noReduce(commentRepo.dislike(action.comment.id))
        is PlayerAction.Reply -> noReduce { sendEvent(PlayerEvent.PostComment(video.id, action.parent.id)) }
        is PlayerAction.ViewReplies -> noReduce { sendEvent(PlayerEvent.DisplayReplies(action.comment.id)) }
        is PlayerAction.ViewUser -> Observable.fromCallable {
            sendEvent(PlayerEvent.DisplayUser(action.user))
            PlayerReducer.SetViewMode(ViewMode.Collapsed)
        }
        is PlayerAction.ToggleFullscreen -> Observable.just(PlayerReducer.ToggleFullScreen)
        PlayerAction.PostComment -> noReduce { sendEvent(PlayerEvent.PostComment(video.id)) }
        is PlayerAction.SetQuality -> noReduce(player.setQuality(action.quality))
        is PlayerAction.SetViewMode -> {
            if (action.viewMode == ViewMode.Dismissed)
                player.stop().andThen(Observable.just(PlayerReducer.SetViewMode(action.viewMode)))
            else
                Observable.just(PlayerReducer.SetViewMode(action.viewMode))
        }
        PlayerAction.Pause -> noReduce(player.pause())
        PlayerAction.TogglePlayPause -> noReduce(player.togglePlayPause())
        is PlayerAction.PlayVideo -> throw RuntimeException("Should not reach this branch !(PlayerAction.PlayVideo)")
        is PlayerAction.SeekTo -> noReduce(player.seekTo(action.position))
    }

    private fun loadVideoContents(video: Video): Observable<PlayerReducer> {
        val (commentPages, commentStates) = commentRepo.getComments(video.id)
        return Observable.merge(listOf(videoRepo.getRelatedVideos(video.id).map<PlayerReducer>(PlayerReducer::DisplayRelatedVideo).onErrorReturnItem(PlayerReducer.DisplayRelatedVideosError(UiError(PlayerErrors.General.message))),
                player.currentPosition.map<PlayerReducer>(PlayerReducer::UpdatePosition),
                player.duration.map<PlayerReducer>(PlayerReducer::UpdateDuration),
                player.supportedQuality.map<PlayerReducer>(PlayerReducer::DisplayQualityLevels),
                player.selectedQualityLevel.map<PlayerReducer>(PlayerReducer::DisplayCurrentQualityLevel),
                player.timelinePreviewUrl.map<PlayerReducer>(PlayerReducer::DisplayTimelinePreview),
                player.isPlaying.map(PlayerReducer::UpdatePlayingState),
                commentPages.map<PlayerReducer>(PlayerReducer::DisplayComments),
                commentStates.map(::mapCommentStates)
        ))
    }

    private fun mapCommentStates(pagingState: PagingState): PlayerReducer = when (pagingState) {
        PagingState.InitialFetch -> PlayerReducer.FetchingComments
        PagingState.Fetching -> PlayerReducer.FetchingComments
        PagingState.Fetched -> PlayerReducer.CommentsFetched
        PagingState.Completed -> PlayerReducer.CommentsFetched
        PagingState.Empty -> PlayerReducer.DisplayCommentError(UiError(PlayerErrors.General.message))
        PagingState.Error -> PlayerReducer.DisplayCommentError(UiError(PlayerErrors.General.message))
    }

    private fun handleClick(viewMode: ViewMode) = when (viewMode) {
        ViewMode.Expanded -> ViewMode.Fullscreen
        ViewMode.Fullscreen -> ViewMode.Expanded
        else -> viewMode
    }

    private fun loadUser(): Observable<PlayerReducer> = userRepo.activeUser
            .map { PlayerReducer.DisplayUser(it.entity) }
}