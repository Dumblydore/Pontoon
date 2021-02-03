package me.mauricee.pontoon.ui.main.user

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import me.mauricee.pontoon.model.DataModel
import me.mauricee.pontoon.model.user.User
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.ReduxPresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState

class UserPresenter @AssistedInject constructor(@Assisted private val args: UserArgs,
                                                private val userRepository: UserRepository) : ReduxPresenter<UserState, UserReducer, UserAction, UserEvent>() {

    override fun onViewAttached(view: BaseContract.View<UserState, UserAction>): Observable<UserReducer> {
        val user = userRepository.getUser(args.userId)
        return user.get().map<UserReducer>(UserReducer::UserLoaded)
                .startWith(UserReducer.Loading)
                .mergeWith(view.actions.flatMap { handleAction(user, it) })
    }

    override fun onReduce(state: UserState, reducer: UserReducer): UserState = when (reducer) {
        UserReducer.Loading -> state.copy(uiState = UiState.Loading)
        is UserReducer.UserLoaded -> state.copy(uiState = UiState.Success, user = reducer.user.entity, activity = reducer.user.activities)
        is UserReducer.Error -> state.copy(uiState = UiState.Failed(UiError(reducer.error.msg)))
    }

    private fun handleAction(user: DataModel<User>, action: UserAction): Observable<UserReducer> {
        return when (action) {
            UserAction.Refresh -> user.fetch().map<UserReducer>(UserReducer::UserLoaded).toObservable()
            is UserAction.ActivityClicked -> noReduce { /*action.activity.postId?.let(navigator::playVideo) */}
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(args: UserArgs): UserPresenter
    }
}