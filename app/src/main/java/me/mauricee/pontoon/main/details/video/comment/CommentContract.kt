package me.mauricee.pontoon.main.details.video.comment

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.UserRepository

interface CommentContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        class CurrentUser(val user: UserRepository.User) : State()
        object Close : State()
    }

    sealed class Action : EventTracker.Action {
        class Comment(val text: String, val videoId: String) : Action()
        class Reply(val text: String, val commentId: String, val videoId: String) : Action()
    }
}