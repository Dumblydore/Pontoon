package me.mauricee.pontoon.main.videos

import androidx.annotation.StringRes
import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video

interface VideoContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        object Refresh : Action()
        class Subscription(val creator: UserRepository.Creator) : Action()
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State : EventTracker.State {
        class Loading(clean: Boolean = true) : State()
        class DisplaySubscriptions(val subscriptions: List<UserRepository.Creator>) : State()
        class DisplayVideos(val videos: PagedList<Video>) : State()
        class Error(val type: Type = Type.Unknown) : State() {
            enum class Type(@StringRes val msg: Int) {
                Network(R.string.subscriptions_error_network),
                NoVideos(R.string.subscriptions_error_noSubscriptions),
                Unknown(R.string.subscriptions_error_general)
            }
        }
    }
}