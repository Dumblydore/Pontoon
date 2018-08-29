package me.mauricee.pontoon.main.creatorList

import androidx.annotation.StringRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.UserRepository

interface CreatorListContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        class Creator(val creator: UserRepository.Creator) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class DisplayCreators(val creator: List<UserRepository.Creator>) : State()
        class Error(val type: Type = Type.Unknown) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            enum class Type(@StringRes val msg: Int) {
                Network(R.string.creator_error_noCreator),
                Unsubscribed(R.string.creator_error_noVideos),
                Unknown(R.string.creator_error_general)
            }
        }
    }
}