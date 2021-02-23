package me.mauricee.pontoon.ui.main.player.details.comment

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import me.mauricee.pontoon.repository.comment.CommentRepository
import me.mauricee.pontoon.repository.session.SessionRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiState

class CommentPresenter @AssistedInject constructor(@Assisted private val args: CommentContract.Args,
                                                   private val commentRepository: CommentRepository,
                                                   private val sessionRepo: SessionRepository) : BasePresenter<CommentContract.State, CommentContract.Reducer, CommentContract.Action, CommentContract.Event>() {


    override fun onViewAttached(view: BaseContract.View<CommentContract.Action>): Observable<CommentContract.Reducer> {
        return Observable.merge(sessionRepo.activeUser.map(CommentContract.Reducer::CurrentUser).toObservable(),
                view.actions.flatMap(::handleActions),
                loadReplyingData()
        )
    }

    private fun handleActions(action: CommentContract.Action) =
            when (action) {
                is CommentContract.Action.Comment -> post(args.videoId, action.text, args.commentId)
            }

    override fun onReduce(state: CommentContract.State, reducer: CommentContract.Reducer): CommentContract.State {
        return when (reducer) {
            CommentContract.Reducer.Submitting -> state.copy(submitState = UiState.Loading)
            is CommentContract.Reducer.CurrentUser -> state.copy(submitState = UiState.Success, user = reducer.user)
            is CommentContract.Reducer.ReplyingTo -> state.copy(replyingTo = reducer.user.username)
        }
    }

    private fun loadReplyingData(): Observable<CommentContract.Reducer> {
        return if (args.commentId.isNullOrEmpty()) Observable.empty()
        else commentRepository.getComment(args.commentId).map { CommentContract.Reducer.ReplyingTo(it.user) }
    }

    private fun post(videoId: String, comment: String, parentComment: String?): Observable<CommentContract.Reducer> = commentRepository.postComment(
            comment, videoId, parentComment
    ).andThen(noReduce { sendEvent(CommentContract.Event.Dismiss) })
            .startWith(CommentContract.Reducer.Submitting)

    @AssistedFactory
    interface Factory {
        fun create(args: CommentContract.Args): CommentPresenter
    }
}