package me.mauricee.pontoon.main.details.replies

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository

interface RepliesContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Error : State()
        object Loading : State()
        class Liked(val comment: Comment) : State()
        class Cleared(val comment: Comment) : State()
        class Disliked(val comment: Comment) : State()
        class CurrentUser(val user: UserRepository.User): State()
        class Replies(val parent: Comment, val comments: List<Comment>) : State()
    }

    sealed class Action : EventTracker.Action {
        class Like(val comment: Comment) : Action()
        class Reply(val parent: Comment) : Action()
        class Clear(val comment: Comment) : Action()
        class Parent(val comment: String) : Action()
        class Dislike(val comment: Comment) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
    }

}