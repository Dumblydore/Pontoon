package me.mauricee.pontoon.main.user

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class UserPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                        private val userRepository: UserRepository,
                                        private val navigator: MainContract.Navigator,
                                        eventTracker: EventTracker) :

        UserContract.Presenter, BasePresenter<UserContract.State, UserContract.View>(eventTracker) {

    override fun onViewAttached(view: UserContract.View): Observable<UserContract.State> = view.actions
            .doOnNext { eventTracker.trackAction(it, view) }.flatMap(::handleAction)

    private fun handleAction(action: UserContract.Action): Observable<UserContract.State> = when (action) {
        is UserContract.Action.Refresh -> refresh(action.userId)
        is UserContract.Action.Video -> navigateToVideo(action)
    }

    private fun navigateToVideo(action: UserContract.Action.Video) = videoRepository.getVideo(action.videoId)
            .flatMapObservable { stateless { navigator.playVideo(it) } }


    private fun refresh(userId: String): Observable<UserContract.State> {
        return userRepository.getUsers(userId).map { it.first() }.flatMap {
            userRepository.getActivity(it).map<UserContract.State>(UserContract.State::Activity)
                    .startWith(UserContract.State.User(it))
        }.startWith(UserContract.State.Loading)
    }
}