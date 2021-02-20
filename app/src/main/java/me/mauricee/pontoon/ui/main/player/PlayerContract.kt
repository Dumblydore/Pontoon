package me.mauricee.pontoon.ui.main.player

import android.util.Rational
import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.creator.CreatorEntity
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(p: PlayerPresenter) : EventViewModel<PlayerState, PlayerAction, PlayerEvent>(PlayerState(), p)

sealed class PlayerAction : EventTracker.Action {
    object ViewCreator : PlayerAction()
    object PostComment : PlayerAction()
    object ToggleFullscreen : PlayerAction()
    object TogglePlayPause : PlayerAction()
    object ToggleControls : PlayerAction()
    object Pause : PlayerAction()
    data class SeekTo(val position: Long) : PlayerAction()
    data class SetControlVisibility(val controlsVisible: Boolean) : PlayerAction()
    data class SetViewMode(val viewMode: ViewMode) : PlayerAction()
    data class PlayVideo(val videoId: String, val commentId: String? = null) : PlayerAction()
    data class Like(val comment: Comment) : PlayerAction()
    data class Reply(val parent: Comment) : PlayerAction()
    data class Dislike(val comment: Comment) : PlayerAction()
    data class ViewReplies(val comment: Comment) : PlayerAction()
    data class ViewUser(val user: UserEntity) : PlayerAction()
    data class SetQuality(val quality: Player.Quality) : PlayerAction()
}

data class PlayerState(val videoState: UiState = UiState.Empty,
                       val user: UserEntity? = null,
                       val video: Video? = null,
                       val isPlaying: Boolean? = null,
                       val controlsVisible: Boolean? = null,
                       val isBuffering: Boolean? = null,
                       val timestamp: CharSequence? = null,
                       val position: Long = 0,
                       val duration: Long = 0,
                       val previewImage: String? = null,
                       val relatedVideosState: UiState = UiState.Empty,
                       val relatedVideos: List<Video> = emptyList(),
                       val commentState: UiState = UiState.Empty,
                       val comments: List<Comment> = emptyList(),
                       val currentQualityLevel: Player.Quality? = null,
                       val timelineUrl: String? = null,
                       val qualityLevels: Set<Player.Quality> = emptySet(),
                       val viewMode: ViewMode = ViewMode.Dismissed,
                       val playerRatio: String = "16:9")

sealed class PlayerReducer {
    object Loading : PlayerReducer()
    object FetchingComments : PlayerReducer()
    object CommentsFetched : PlayerReducer()
    object ToggleFullScreen : PlayerReducer()
    data class ControlsVisible(val controlsVisible: Boolean) : PlayerReducer()
    data class UpdatePosition(val position: Long) : PlayerReducer()
    data class UpdateDuration(val duration: Long) : PlayerReducer()
    data class UpdatePlayingState(val isPlaying: Boolean) : PlayerReducer()
    data class DisplayUser(val user: UserEntity) : PlayerReducer()
    data class DisplayTimelinePreview(val previewUrl: String) : PlayerReducer()
    data class DisplayCurrentQualityLevel(val qualityLevel: Player.Quality) : PlayerReducer()
    data class DisplayQualityLevels(val qualityLevels: Set<Player.Quality>) : PlayerReducer()
    data class DisplayPreview(val previewUrl: String) : PlayerReducer()
    data class DisplayBuffer(val displayBuffer: Boolean) : PlayerReducer()
    data class DisplayComments(val comments: List<Comment>) : PlayerReducer()
    data class DisplayVideo(val video: Video) : PlayerReducer()
    data class DisplayRelatedVideo(val videos: List<Video>) : PlayerReducer()
    data class UpdateViewMode(val viewMode: ViewMode) : PlayerReducer()
    data class DisplayVideoError(val error: UiError) : PlayerReducer()
    data class DisplayRelatedVideosError(val error: UiError) : PlayerReducer()
    data class DisplayCommentError(val error: UiError) : PlayerReducer()
    data class SetViewMode(val viewMode: ViewMode) : PlayerReducer()
    data class SetPlayerRatio(val playerRatio: Rational) : PlayerReducer()
}

sealed class PlayerEvent {
    data class PostComment(val videoId: String, val comment: String? = null) : PlayerEvent()
    data class DisplayReplies(val commentId: String) : PlayerEvent()
    object OnCommentSuccess : PlayerEvent()
    object OnCommentError : PlayerEvent()
    data class DisplayCreator(val creator: CreatorEntity) : PlayerEvent()
    data class DisplayUser(val user: UserEntity) : PlayerEvent()
}

enum class ViewMode {
    Dismissed,
    Collapsed,
    Expanded,
    Fullscreen,
    PictureInPicture
}

enum class PlayerErrors(@StringRes val message: Int) {
    NoComments(R.string.details_error_noComments),
    NoRelatedVideos(R.string.details_error_noRelatedVideos),
    General(R.string.details_error_general),
    Like(R.string.details_error_like),
    Dislike(R.string.details_error_dislike)
}