package me.mauricee.pontoon.main.user

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class UserPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                        private val commentRepository: CommentRepository,
                                        private val userRepository: UserRepository,
                                        private val navigator: MainContract.Navigator,
                                        eventTracker: EventTracker) :

        UserContract.Presenter, BasePresenter<UserContract.State, UserContract.View>(eventTracker) {

    override fun onViewAttached(view: UserContract.View): Observable<UserContract.State> =
            view.actions.flatMap(::handleAction)

    private fun handleAction(action: UserContract.Action): Observable<UserContract.State> = when (action) {
        is UserContract.Action.Refresh -> refresh(action.userId)
        is UserContract.Action.Context -> navigateToComment(action.comment)
    }

    private fun refresh(userId: String): Observable<UserContract.State> {
        return userRepository.getUsers(userId).map { it.first() }.map { UserContract.State.User(it) }
    }

    private fun navigateToComment(comment: Comment): Observable<UserContract.State> {
//        navigator.playVideo(comment.video, comment.id)
        return Observable.empty()
    }
}