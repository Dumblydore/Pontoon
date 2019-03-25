package me.mauricee.pontoon.main

import androidx.annotation.IdRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.livestream.LiveStreamInfo
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video

interface MainContract {
    interface View : BaseContract.View<MainContract.State, MainContract.Action> {
        override val name: String
            get() = "Main"
    }

    interface Presenter : BaseContract.Presenter<MainContract.View>

    sealed class State : EventTracker.State {
        object Logout : State()
        object SessionExpired : State()
        data class NightMode(val isInNightMode: Boolean) : State()
        data class CurrentUser(val user: UserRepository.User, val subCount: Int) : State()
    }

    sealed class Action : EventTracker.Action {
        object SuccessfulLogout : Action()
        object Expired : Action()
        object Preferences : Action()
        object Profile : Action()
        object NightMode : Action()
        object PlayerClicked : Action()
        data class PlayVideo(val videoId: String) : Action()
        data class PlayLivestream(val creatorId: String) : Action()


        companion object {
            fun fromNavDrawer(@IdRes id: Int) = when (id) {
                R.id.action_logout -> SuccessfulLogout
                R.id.action_prefs -> Preferences
                R.id.action_profile -> Profile
                R.id.action_dayNight -> NightMode
                else -> throw RuntimeException("Invalid Navigation Drawer option")
            }
        }
    }

    interface Navigator {

//        val optionsBottomSheet: OptionsBottomSheetView

        fun showMessage(content: String, action: Pair<String, () -> Unit>)

        fun toPreferences()

        fun toCreator(creator: UserRepository.Creator)

        fun toCreatorsList()

        fun toUser(user: UserRepository.User)

        fun playVideo(video: Video, commentId: String = "")

        fun playLiveStream(liveStream: LiveStreamInfo)

        fun setMenuExpanded(isExpanded: Boolean)
    }
}