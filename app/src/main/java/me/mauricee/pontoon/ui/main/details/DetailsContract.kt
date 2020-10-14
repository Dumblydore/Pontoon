package me.mauricee.pontoon.ui.main.details

import androidx.annotation.StringRes
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.Video


interface DetailsContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    interface Navigator {
        fun comment(videoId: String, comment: String? = null)
        fun displayReplies(commentId: String)
        fun onCommentSuccess()
        fun onCommentError()
    }

    sealed class Action : EventTracker.Action {
        object ViewCreator : Action()
        object PostComment : Action()
        class PlayVideo(val id: String) : Action()
        class Like(val comment: Comment) : Action()
        class Reply(val parent: Comment) : Action()
        class Dislike(val comment: Comment) : Action()
        class ViewReplies(val comment: Comment) : Action()
        class ViewUser(val user: UserEntity) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class VideoInfo(val video: Video) : State()
        class Like(val comment: Comment) : State()
        class Dislike(val comment: Comment) : State()
        class Comments(val comments: List<Comment>) : State()
        class CurrentUser(val user: User) : State()
        class RelatedVideos(val relatedVideos: List<Video>) : State()
        class Error(val type: ErrorType = ErrorType.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
        }
    }

    sealed class ContentItem(override val id: Int) : Diffable<Int> {
        data class VideoInfo(val video: Video) : ContentItem(0)
        object PostComment : ContentItem(1)
    }

    enum class ErrorType(@StringRes val message: Int) {
        NoComments(R.string.details_error_noComments),
        NoRelatedVideos(R.string.details_error_noRelatedVideos),
        General(R.string.details_error_general),
        Like(R.string.details_error_like),
        Dislike(R.string.details_error_dislike)
    }
}

