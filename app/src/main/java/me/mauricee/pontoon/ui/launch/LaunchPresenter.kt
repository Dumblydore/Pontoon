package me.mauricee.pontoon.ui.launch

import io.reactivex.Observable
import me.mauricee.pontoon.repository.session.LoginResult
import me.mauricee.pontoon.repository.session.SessionRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

class LaunchPresenter @Inject constructor(private val sessionRepository: SessionRepository) : BasePresenter<LaunchState, LaunchReducer, LaunchAction, LaunchEvent>() {

    override fun onViewAttached(view: BaseContract.View<LaunchAction>): Observable<LaunchReducer> {
        return sessionRepository.canLogin().flatMapObservable { canLogin ->
            if (canLogin)
                attemptLogin()
            else
                noReduce { sendEvent(LaunchEvent.ToLogin(false)) }
        }
    }

    private fun attemptLogin(): Observable<LaunchReducer> = sessionRepository.loginWithStoredCredentials()
            .flatMapObservable { result ->
                noReduce {
                    val event = when (result) {
                        LoginResult.Requires2FA -> LaunchEvent.ToLogin(true)
                        is LoginResult.Error -> LaunchEvent.ToLogin(false)
                        LoginResult.Success -> LaunchEvent.ToSession
                    }
                    sendEvent(event)
                }
            }

    override fun onReduce(state: LaunchState, reducer: LaunchReducer): LaunchState = when (reducer) {
        LaunchReducer.LoggingIn -> state.copy(uiState = UiState.Loading)
    }
}