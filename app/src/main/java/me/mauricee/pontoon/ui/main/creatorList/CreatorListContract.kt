package me.mauricee.pontoon.ui.main.creatorList

import androidx.annotation.StringRes
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.creator.Creator

interface CreatorListContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        class CreatorSelected(val creator: Creator) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class DisplayCreators(val creator: List<Creator>) : State()
        class Error(val type: Type = Type.Unknown) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR
            enum class Type(@StringRes val msg: Int) {
                Network(R.string.creator_error_noCreator),
                Unsubscribed(R.string.creator_error_noVideos),
                Unknown(R.string.creator_error_general)
            }
        }
    }
}