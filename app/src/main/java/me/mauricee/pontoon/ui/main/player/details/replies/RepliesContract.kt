package me.mauricee.pontoon.ui.main.player.details.replies

import androidx.annotation.StringRes
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.ChildComment
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.user.UserEntity

interface RepliesContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Loading : State()
        class Error(val type: ErrorType = ErrorType.General) : State()
        class Liked(val comment: Comment) : State()
        class Cleared(val comment: Comment) : State()
        class Disliked(val comment: Comment) : State()
        class CurrentUser(val user: User) : State()
        class Replies(val parent: Comment, val comments: List<ChildComment>) : State()
    }

    sealed class Action : EventTracker.Action {
        class Like(val comment: Comment) : Action()
        class Reply(val parent: Comment) : Action()
        class Clear(val comment: Comment) : Action()
        class Parent(val comment: String) : Action()
        class Dislike(val comment: Comment) : Action()
        class ViewUser(val user: UserEntity) : Action()
    }

    enum class ErrorType(@StringRes val msg: Int) {
        General(R.string.replies_error),
        Like(R.string.replies_error_like),
        Dislike(R.string.replies_error_dislike),
        Cleared(R.string.replies_error_cleared),
    }

}