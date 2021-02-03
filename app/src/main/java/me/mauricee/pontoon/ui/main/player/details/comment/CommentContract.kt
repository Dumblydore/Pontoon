package me.mauricee.pontoon.ui.main.player.details.comment

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState

interface CommentContract {

    data class Args(val videoId: String, val commentId: String?)

    data class State(val submitState: UiState = UiState.Empty,
                     val replyingTo: String? = null,
                     val user: User? = null)

    sealed class Reducer {
        object Submitting : Reducer()
        data class ReplyingTo(val user: UserEntity) : Reducer()
        data class CurrentUser(val user: User) : Reducer()
    }

    sealed class Event {
        object Dismiss : Event()
    }

    sealed class Action : EventTracker.Action {
        class Comment(val text: String) : Action()
    }

    class ViewModel @AssistedInject constructor(@Assisted p: CommentPresenter) : EventViewModel<State, Action, Event>(State(), p){
        @AssistedFactory
        interface Factory {
            fun create(p: CommentPresenter): ViewModel
        }
    }
}