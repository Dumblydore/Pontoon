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
            is CommentContract.Action.Comment -> commentRepository.comment(action.text, action.videoId)
                    .flatMap { closeAfterAction { navigator.onCommentSuccess() } }
            is CommentContract.Action.Reply -> commentRepository.comment(action.text, action.commentId, action.videoId)
                    .flatMap { closeAfterAction { navigator.onCommentSuccess() } }
        }.onErrorResumeNext { ig: Throwable -> closeAfterAction { navigator.onCommentError() } }
    }.startWith(CommentContract.State.CurrentUser(userRepository.activeUser))

    private fun closeAfterAction(action: () -> Unit): Observable<CommentContract.State> {
        action()
        return CommentContract.State.Close.toObservable()
    }
}