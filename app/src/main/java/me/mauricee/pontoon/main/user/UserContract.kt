package me.mauricee.pontoon.main.user

import androidx.annotation.StringRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository

class UserContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Loading : State()
        class User(val user: UserRepository.User) : State()
        class Activity(val activity: List<UserRepository.Activity>) : State()
        class Error(val type: Type) : State() {
            override val tag: String
                get() = "${super.tag}_$type"

            enum class Type(@StringRes msg: Int) {
                NoActivity(R.string.user_error_noActivity),
                NoPlaylist(R.string.user_error_noPlaylist),
                NoSubscriptions(R.string.user_error_noSubscriptions),
                General(R.string.user_error_general)
            }
        }
    }

    sealed class Action : EventTracker.Action {
        class Refresh(val userId: String) : Action()
        class Video(val comment: Comment) : Action()
    }
}