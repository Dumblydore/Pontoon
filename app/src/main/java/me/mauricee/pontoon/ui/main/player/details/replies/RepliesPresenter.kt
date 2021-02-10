package me.mauricee.pontoon.ui.main.player.details.replies

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState

class RepliesPresenter @AssistedInject constructor(@Assisted private val args: RepliesContract.Args,
                                                   private val commentRepository: CommentRepository,
                                                   private val userRepository: UserRepository) : BasePresenter<RepliesContract.State, RepliesContract.Reducer, RepliesContract.Action, RepliesContract.Event>() {
    override fun onViewAttached(view: BaseContract.View<RepliesContract.Action>): Observable<RepliesContract.Reducer> {
        return Observable.merge(userRepository.activeUser.map(RepliesContract.Reducer::CurrentUser),
                loadReplies()
        )
    }

    private fun handleActions(action: RepliesContract.Action): Observable<RepliesContract.Reducer> {
        return when (action) {
            is RepliesContract.Action.Like -> TODO()
            is RepliesContract.Action.Reply -> TODO()
            is RepliesContract.Action.Clear -> TODO()
            is RepliesContract.Action.Dislike -> TODO()
        }
    }

    private fun loadReplies() = commentRepository.getComment(args.commentId)
            .map<RepliesContract.Reducer> { RepliesContract.Reducer.Replies(it, it.replies) }
            .onErrorReturnItem(RepliesContract.Reducer.Error())

    //    override fun onViewAttached(view: RepliesContract.View): Observable<RepliesContract.State> = view.actions.flatMap(::handleActions)
//            .mergeWith(userRepository.activeUser.map(RepliesContract.State::CurrentUser))
//            .onErrorReturnItem(RepliesContract.State.Error())
//
//    private fun handleActions(action: RepliesContract.Action): Observable<RepliesContract.State> = when (action) {
//        is RepliesContract.Action.Like -> stateless(commentRepository.like(action.comment.id)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Like))
//        is RepliesContract.Action.Dislike -> stateless(commentRepository.dislike(action.comment.id)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Dislike))
//        is RepliesContract.Action.Clear -> stateless(commentRepository.clear(action.comment.id)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Cleared))
//        is RepliesContract.Action.Reply -> stateless {/* navigator.comment(player.currentlyPlaying!!.video.id, action.parent.id) */ }
//        is RepliesContract.Action.Parent -> commentRepository.getComment(action.comment).map<RepliesContract.State> { RepliesContract.State.Replies(it, it.replies) }.startWith(RepliesContract.State.Loading).onErrorReturnItem(RepliesContract.State.Error())
//        is RepliesContract.Action.ViewUser -> stateless { /*mainNavigator.toUser(action.user.id) */ }
//    }

    override fun onReduce(state: RepliesContract.State, reducer: RepliesContract.Reducer): RepliesContract.State = when (reducer) {
        RepliesContract.Reducer.Loading -> state.copy(uiState = UiState.Loading)
        is RepliesContract.Reducer.Error -> state.copy(uiState = UiState.Failed(UiError(reducer.type.msg)))
        is RepliesContract.Reducer.CurrentUser -> state.copy(user = reducer.user)
        is RepliesContract.Reducer.Replies -> state.copy(uiState = UiState.Success, parentComment = reducer.parent, comments = reducer.comments)
    }

    @AssistedFactory
    interface Factory {
        fun create(p: RepliesContract.Args): RepliesPresenter
    }
}