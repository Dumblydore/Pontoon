package me.mauricee.pontoon.main

import androidx.annotation.IdRes
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video

interface MainContract {
    interface View : BaseContract.View<MainContract.State, MainContract.Action>
    interface Presenter : BaseContract.Presenter<MainContract.View>

    sealed class State {
        class CurrentUser(val user: UserRepository.User) : State()
        object Preferences : State()
        object Logout : State()
    }

    sealed class Action {
        object Logout : Action()
        object Preferences : Action()
        object Profile : Action()
        object ClickEvent : Action()


        companion object {
            fun fromNavDrawer(@IdRes id: Int) = when (id) {
                R.id.action_logout -> Logout
                R.id.action_prefs -> Preferences
                R.id.action_profile -> Profile
                else -> throw RuntimeException("Invalid Navigation Drawer option")
            }
        }
    }

    interface Navigator {

//        val optionsBottomSheet: OptionsBottomSheetView

        fun toCreator(user: UserRepository.Creator)

        fun toUser(user: UserRepository.User)

        fun playVideo(video: Video, commentId: String = "")

        fun setPlayerExpanded(isExpanded: Boolean)

    }
}