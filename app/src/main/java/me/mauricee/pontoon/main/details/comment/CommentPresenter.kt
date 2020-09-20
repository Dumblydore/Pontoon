package me.mauricee.pontoon.main.details.comment

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.main.details.DetailsContract
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class CommentPresenter @Inject constructor(private val commentRepository: CommentRepository,
                                           private val userRepository: UserRepository,
                                           private val navigator: DetailsContract.Navigator,
                                           eventTracker: EventTracker)
    : CommentContract.Presenter, BasePresenter<CommentContract.State, CommentContract.View>(eventTracker) {

    override fun onViewAttached(view: CommentContract.View): Observable<CommentContract.State> = view.actions.flatMap<CommentContract.State> { action ->
        when (action) {
            is CommentContract.Action.Comment -> commentRepository.postComment(action.text, action.videoId)
                    .andThen(closeAfterAction { navigator.onCommentSuccess() })
            is CommentContract.Action.Reply -> commentRepository.postComment(action.text, action.commentId, action.videoId)
                    .andThen(closeAfterAction { navigator.onCommentSuccess() })
        }.onErrorResumeNext { _: Throwable -> closeAfterAction { navigator.onCommentError() } }
    }.mergeWith(userRepository.activeUser.map(CommentContract.State::CurrentUser))


    private fun closeAfterAction(action: () -> Unit): Observable<CommentContract.State> {
        action()
        return CommentContract.State.Close.toObservable()
    }
}