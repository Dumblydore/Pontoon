package me.mauricee.pontoon.ui.main.player

import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(p: PlayerPresenter) : EventViewModel<PlayerState, PlayerAction, PlayerEvent>(PlayerState(), p)

sealed class PlayerAction : EventTracker.Action {
    object ViewCreator : PlayerAction()
    object PostComment : PlayerAction()
    class PlayVideo(val videoId: String, val commentId: String? = null) : PlayerAction()
    class Like(val comment: Comment) : PlayerAction()
    class Reply(val parent: Comment) : PlayerAction()
    class Dislike(val comment: Comment) : PlayerAction()
    class ViewReplies(val comment: Comment) : PlayerAction()
    class ViewUser(val user: UserEntity) : PlayerAction()
    class SetViewMode(val viewMode: ViewMode) : PlayerAction()
    class SetQuality(val quality: NewPlayer.Quality) : PlayerAction()
}

data class PlayerState(val videoState: UiState = UiState.Empty,
                       val user: UserEntity? = null,
                       val video: Video? = null,
                       val relatedVideosState: UiState = UiState.Empty,
                       val relatedVideos: List<Video> = emptyList(),
                       val commentState: UiState = UiState.Empty,
                       val comments: List<Comment> = emptyList(),
                       val qualityLevels: Set<NewPlayer.Quality> = emptySet(),
                       val viewMode: ViewMode = ViewMode.Dismissed)

sealed class PlayerReducer {
    object Loading : PlayerReducer()
    object FetchingComments : PlayerReducer()
    object CommentsFetched : PlayerReducer()
    data class DisplayUser(val user: UserEntity) : PlayerReducer()
    data class DisplayQualityLevels(val qualityLevels: Set<NewPlayer.Quality>) : PlayerReducer()
    data class DisplayComments(val comments: List<Comment>) : PlayerReducer()
    data class DisplayVideo(val video: Video) : PlayerReducer()
    data class DisplayRelatedVideo(val videos: List<Video>) : PlayerReducer()
    object ToggleViewMode : PlayerReducer()
    data class UpdateViewMode(val viewMode: ViewMode) : PlayerReducer()
    data class DisplayVideoError(val error: UiError) : PlayerReducer()
    data class DisplayRelatedVideosError(val error: UiError) : PlayerReducer()
    data class DisplayCommentError(val error: UiError) : PlayerReducer()
}

sealed class PlayerEvent {
    data class PostComment(val videoId: String, val comment: String? = null) : PlayerEvent()
    data class DisplayReplies(val commentId: String) : PlayerEvent()

    //    data class DisplayCreator(val creator: Creator) : PlayerEvent()
    data class DisplayUser(val user: UserEntity) : PlayerEvent()
    object OnCommentSuccess : PlayerEvent()
    object OnCommentError : PlayerEvent()
}

enum class ViewMode {
    Dismissed,
    Expanded,
    Fullscreen
}

enum class PlayerErrors(@StringRes val message: Int) {
    NoComments(R.string.details_error_noComments),
    NoRelatedVideos(R.string.details_error_noRelatedVideos),
    General(R.string.details_error_general),
    Like(R.string.details_error_like),
    Dislike(R.string.details_error_dislike)
}