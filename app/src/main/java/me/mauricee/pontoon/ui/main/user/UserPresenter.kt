package me.mauricee.pontoon.ui.main.user

import io.reactivex.Observable
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ui.main.MainContract
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class UserPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                        private val userRepository: UserRepository,
                                        private val navigator: MainContract.Navigator,
                                        eventTracker: EventTracker) :

        UserContract.Presenter, BasePresenter<UserContract.State, UserContract.View>(eventTracker) {

    override fun onViewAttached(view: UserContract.View): Observable<UserContract.State> = view.actions
            .doOnNext { eventTracker.trackAction(it, view) }.switchMap(::handleAction)
            .onErrorReturnItem(UserContract.State.Error())

    private fun handleAction(action: UserContract.Action): Observable<UserContract.State> = when (action) {
        is UserContract.Action.Refresh -> refresh(action.userId)
        is UserContract.Action.Video -> navigateToVideo(action)
    }

    private fun navigateToVideo(action: UserContract.Action.Video) = stateless { navigator.playVideo(action.videoId) }
            .onErrorReturnItem(UserContract.State.Error(UserContract.State.Error.Type.PlaybackFailed))

    private fun refresh(userId: String): Observable<UserContract.State> = userRepository.getUser(userId)
            .map<UserContract.State>(UserContract.State::DisplayUser)
            .onErrorReturnItem(UserContract.State.Error(UserContract.State.Error.Type.User))
            .startWith(UserContract.State.Loading)
}