package me.mauricee.pontoon.main.user

import androidx.annotation.StringRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.UserRepository

class UserContract {

    interface View : BaseContract.View<State, Action>
    interface Presenter : BaseContract.Presenter<View>

    sealed class State : EventTracker.State {
        object Loading : State()
        class User(val user: UserRepository.User) : State()
        class Activity(val activity: List<UserRepository.Activity>) : State()
        class Error(val type: Type = Type.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            enum class Type(@StringRes msg: Int) {
                Activity(R.string.user_error_activity),
                User(R.string.user_error_user),
                PlaybackFailed(R.string.user_error_playback),
                General(R.string.user_error_general)
            }
        }
    }

    sealed class Action : EventTracker.Action {
        class Refresh(val userId: String) : Action()
        class Video(val videoId: String) : Action()
    }
}