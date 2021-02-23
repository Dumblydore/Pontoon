package me.mauricee.pontoon.ui.main.player.details.replies

import androidx.annotation.StringRes
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.repository.comment.Comment
import me.mauricee.pontoon.repository.user.User
import me.mauricee.pontoon.ui.BaseViewModel
import me.mauricee.pontoon.ui.UiState

interface RepliesContract {

    data class Args(val commentId: String)

    data class State(val uiState: UiState = UiState.Empty,
                     val parentComment: Comment? = null,
                     val comments: List<Comment> = emptyList(),
                     val user: User? = null)

    sealed class Reducer {
        object Loading : Reducer()
        data class Error(val type: ErrorType = ErrorType.General) : Reducer()
        data class CurrentUser(val user: User) : Reducer()
        data class Replies(val parent: Comment, val comments: List<Comment>) : Reducer()
    }

    sealed class Action : EventTracker.Action {
        class Like(val comment: Comment) : Action()
        class Reply(val parent: Comment) : Action()
        class Clear(val comment: Comment) : Action()
        class Dislike(val comment: Comment) : Action()
    }

    sealed class Event {
        data class Liked(val comment: Comment) : Event()
        data class Cleared(val comment: Comment) : Event()
        data class Disliked(val comment: Comment) : Event()
    }

    enum class ErrorType(@StringRes val msg: Int) {
        General(R.string.replies_error),
        Like(R.string.replies_error_like),
        Dislike(R.string.replies_error_dislike),
        Cleared(R.string.replies_error_cleared),
    }

    class ViewModel @AssistedInject constructor(@Assisted p: RepliesPresenter) : BaseViewModel<State, Action, Event>(State(), p) {
        @AssistedFactory
        interface Factory {
            fun create(p: RepliesPresenter): ViewModel
        }
    }

}
