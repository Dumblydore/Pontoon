package me.mauricee.pontoon.ui.launch

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx2.await
import me.mauricee.pontoon.repository.session.LoginResult
import me.mauricee.pontoon.repository.session.SessionRepository
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(private val sessionRepository: SessionRepository) : ViewModel() {

    private val _events = MutableSharedFlow<LaunchEvent>(extraBufferCapacity = 1)
    val events: Flow<LaunchEvent> = _events
    val state: Flow<UiState> = flow {
        emit(UiState.Loading)
        _events.emit(attemptToLogin())
        emit(UiState.Success)
    }

    private suspend fun attemptToLogin(): LaunchEvent = if (sessionRepository.canLogin()) {
        when (sessionRepository.loginWithStoredCredentials().await()) {
            LoginResult.Requires2FA -> LaunchEvent.ToLogin(true)
            is LoginResult.Error -> LaunchEvent.ToLogin(false)
            LoginResult.Success -> LaunchEvent.ToSession
        }
    } else LaunchEvent.ToLogin(false)
}
