package me.mauricee.pontoon.ui.main.user

import androidx.annotation.StringRes
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import me.mauricee.pontoon.ui.BaseViewModel
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject


data class UserArgs(val userId: String)

data class UserState(val uiState: UiState = UiState.Empty,
                     val user: UserEntity? = null,
                     val activity: List<ActivityEntity> = emptyList())

sealed class UserReducer {
    object Loading : UserReducer()
    data class UserLoaded(val user: User) : UserReducer()
    data class Error(val error: UserError) : UserReducer()
}

sealed class UserAction : EventTracker.Action {
    object Refresh : UserAction()
    data class ActivityClicked(val activity: ActivityEntity) : UserAction()
}

typealias UserEvent = Nothing

enum class UserError(@StringRes val msg: Int) {
    User(R.string.user_error_user),
    PlaybackFailed(R.string.user_error_playback),
    General(R.string.user_error_general)
}

class UserViewModel(p: UserPresenter) : EventViewModel<UserState, UserAction, UserEvent>(UserState(), p) {
    class Factory @Inject constructor(p: UserPresenter) : BaseViewModel.Factory<UserViewModel>({ UserViewModel(p) })
}