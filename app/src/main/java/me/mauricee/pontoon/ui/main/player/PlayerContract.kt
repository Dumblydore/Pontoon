package me.mauricee.pontoon.ui.main.player

import androidx.annotation.StringRes
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.BaseViewModel
import javax.inject.Inject

class PlayerViewModel(p: PlayerPresenter) : BaseViewModel<PlayerState, PlayerAction>(PlayerState(), p) {
    class Factory @Inject constructor(p: PlayerPresenter) : BaseViewModel.Factory<PlayerViewModel>({ PlayerViewModel(p) })
}

interface PlayerNavigator {
    fun comment(videoId: String, comment: String? = null)
    fun displayReplies(commentId: String)
    fun onCommentSuccess()
    fun onCommentError()
}

sealed class PlayerAction : EventTracker.Action {
    object ViewCreator : PlayerAction()
    class PlayVideo(val videoId: String, val commentId: String? = null) : PlayerAction()
    class Like(val comment: Comment) : PlayerAction()
    class Reply(val parent: Comment) : PlayerAction()
    class Dislike(val comment: Comment) : PlayerAction()
    class ViewReplies(val comment: Comment) : PlayerAction()
    class ViewUser(val user: UserEntity) : PlayerAction()
    class SetViewMode(val viewMode: ViewMode) : PlayerAction()
}

data class PlayerState(val isLoading: Boolean = true,
                       val video: Video? = null,
                       val relatedVideos: List<Video> = emptyList(),
                       val comments: List<Comment> = emptyList(),
                       val errors: List<PlayerErrors> = emptyList(),
                       val viewMode: ViewMode = ViewMode.None(false))

sealed class ViewMode {
    data class None(val dismissed: Boolean) : ViewMode()
    data class Scale(val percent: Float) : ViewMode()
    data class Swipe(val percent: Float) : ViewMode()
    object PictureInPicture : ViewMode()
    data class FullScreen(val controlsEnabled: Boolean) : ViewMode()
    data class Expanded(val controlsEnabled: Boolean) : ViewMode()
}

enum class PlayerErrors(@StringRes val message: Int) {
    NoComments(R.string.details_error_noComments),
    NoRelatedVideos(R.string.details_error_noRelatedVideos),
    General(R.string.details_error_general),
    Like(R.string.details_error_like),
    Dislike(R.string.details_error_dislike)
}