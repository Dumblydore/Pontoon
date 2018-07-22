package me.mauricee.pontoon.main.user

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository

class UserContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State {
        class User(val user: UserRepository.User) : State()
        class Comments(val comments: List<Comment>) : State()
    }

    sealed class Action {
        class Refresh(val userId: String) : Action()
        class Context(val comment: Comment) : Action()
    }
}