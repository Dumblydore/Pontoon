package me.mauricee.pontoon.main.details.replies

import androidx.annotation.StringRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository

interface RepliesContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Loading : State()
        class Error(val type: ErrorType = ErrorType.General) : State()
        class Liked(val comment: CommentRepository.NewComment) : State()
        class Cleared(val comment: CommentRepository.NewComment) : State()
        class Disliked(val comment: CommentRepository.NewComment) : State()
        class CurrentUser(val user: UserRepository.User) : State()
        class Replies(val parent: CommentRepository.NewComment, val comments: List<CommentRepository.NewComment>) : State()
    }

    sealed class Action : EventTracker.Action {
        class Like(val comment: CommentRepository.NewComment) : Action()
        class Reply(val parent: CommentRepository.NewComment) : Action()
        class Clear(val comment: CommentRepository.NewComment) : Action()
        class Parent(val comment: String) : Action()
        class Dislike(val comment: CommentRepository.NewComment) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
    }

    enum class ErrorType(@StringRes val msg: Int) {
        General(R.string.replies_error),
        Like(R.string.replies_error_like),
        Dislike(R.string.replies_error_dislike),
        Cleared(R.string.replies_error_cleared),
    }

}