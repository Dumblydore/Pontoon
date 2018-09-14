package me.mauricee.pontoon.main.videos

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video

interface VideoContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        object Refresh : Action()
        class Subscription(val creator: UserRepository.Creator) : Action()
        class PlayVideo(val video: Video) : Action()
        object Creators : Action()
    }

    sealed class State : EventTracker.State {
        class Loading(val clean: Boolean = true) : State() {
            override val tag: String
                get() = "${super.tag}_${if (clean) "clean" else "paginate"}"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR
        }

        class DisplaySubscriptions(val subscriptions: List<UserRepository.Creator>) : State()
        class DisplayVideos(val videos: PagedList<Video>) : State()
        object FinishPageFetch : State()
        class FetchError(val type: Type = Type.Unknown, val retry: () -> Unit) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            enum class Type(@StringRes val msg: Int) {
                Network(R.string.subscriptions_error_network),
                NoVideos(R.string.subscriptions_error_noVideos),
                Unknown(R.string.subscriptions_error_general)
            }
        }

        class Error(val type: Type = Type.Unknown) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
            override val level: EventTracker.Level
                get() = EventTracker.Level.ERROR

            enum class Type(@StringRes val msg: Int) {
                Network(R.string.subscriptions_error_network),
                NoSubscriptions(R.string.subscriptions_error_noSubscriptions),
                Unknown(R.string.subscriptions_error_general)
            }
        }
    }
}