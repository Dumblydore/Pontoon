package me.mauricee.pontoon.ui.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

data class LaunchState(val uiState: UiState = UiState.Empty)

typealias LaunchAction = Nothing

sealed class LaunchEvent {
    data class ToLogin(val initializeWith2Fa: Boolean) : LaunchEvent()
    object ToSession : LaunchEvent()
}

sealed class LaunchReducer {
    object LoggingIn : LaunchReducer()
}


@HiltViewModel
class LaunchViewModel @Inject constructor(p: LaunchPresenter) : EventViewModel<LaunchState, LaunchAction, LaunchEvent>(LaunchState(), p)