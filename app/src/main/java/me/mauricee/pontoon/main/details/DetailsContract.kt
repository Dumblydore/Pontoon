package me.mauricee.pontoon.main.details

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video


private typealias CommentModel = CommentRepository.NewComment

interface DetailsContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    interface Navigator {
        fun comment(video: Video, comment: Comment? = null)
        fun displayReplies(parent: Comment)
        fun onCommentSuccess()
        fun onCommentError()
    }

    sealed class Action : EventTracker.Action {
        object ViewCreator : Action()
        object Comment : Action()
        class PlayVideo(val id: String) : Action()
        class Like(val comment: CommentModel) : Action()
        class Reply(val parent: CommentModel) : Action()
        class Dislike(val comment: CommentModel) : Action()
        class ViewReplies(val comment: CommentModel) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        object Fetching : State()
        object Fetched : State()
        object Finished : State()
        class VideoInfo(val video: Video) : State()
        class Like(val comment: CommentModel) : State()
        class Dislike(val comment: CommentModel) : State()
        class Comments(val comments: PagedList<CommentRepository.NewComment>) : State()
        class CurrentUser(val user: UserRepository.User) : State()
        class RelatedVideos(val relatedVideos: List<Video>) : State()
        class Error(val type: ErrorType = ErrorType.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
        }
    }

    enum class ErrorType(@StringRes val message: Int) {
        FailedFetch(R.string.details_error_noComments),
        NoComments(R.string.details_error_noComments),
        NoRelatedVideos(R.string.details_error_noRelatedVideos),
        General(R.string.details_error_general),
        Like(R.string.details_error_like),
        Dislike(R.string.details_error_dislike)
    }
}

