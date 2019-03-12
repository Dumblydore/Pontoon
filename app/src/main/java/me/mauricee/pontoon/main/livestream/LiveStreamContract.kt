package me.mauricee.pontoon.main.details.livestream

import androidx.annotation.StringRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata
import me.mauricee.pontoon.model.comment.Comment


private typealias CommentModel = Comment

interface LiveStreamContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        data class ViewLiveStream(val creatorId: String) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        data class IsOffline(val metadata: LiveStreamMetadata.Offline) : State()
        data class IsOnline(val metadata: LiveStreamMetadata) : State()
        data class Error(val type: ErrorType = ErrorType.General) : State()
    }

    enum class ErrorType(@StringRes val message: Int) {
        NoComments(R.string.details_error_noComments),
        NoRelatedVideos(R.string.details_error_noRelatedVideos),
        General(R.string.details_error_general),
        Like(R.string.details_error_like),
        Dislike(R.string.details_error_dislike)
    }
}

