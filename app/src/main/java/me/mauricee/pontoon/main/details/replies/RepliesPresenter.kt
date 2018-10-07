package me.mauricee.pontoon.main.details.replies

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.details.DetailsContract
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class RepliesPresenter @Inject constructor(private val commentRepository: CommentRepository,
                                           private val navigator: DetailsContract.Navigator,
                                           private val userRepository: UserRepository,
                                           private val mainNavigator: MainContract.Navigator,
                                           eventTracker: EventTracker) : RepliesContract.Presenter, BasePresenter<RepliesContract.State, RepliesContract.View>(eventTracker) {

    override fun onViewAttached(view: RepliesContract.View): Observable<RepliesContract.State> = view.actions.flatMap(::handleActions)
            .startWith(RepliesContract.State.CurrentUser(userRepository.activeUser))

    private fun handleActions(action: RepliesContract.Action): Observable<RepliesContract.State> = when (action) {
        is RepliesContract.Action.Like -> commentRepository.like(action.comment).map(RepliesContract.State::Liked)
        is RepliesContract.Action.Dislike -> commentRepository.dislike(action.comment).map(RepliesContract.State::Disliked)
        is RepliesContract.Action.Clear -> commentRepository.clear(action.comment).map(RepliesContract.State::Cleared)
        is RepliesContract.Action.Reply -> stateless { navigator.replyToComment(action.parent) }
        is RepliesContract.Action.Parent -> Observable.zip<List<Comment>, Comment, RepliesContract.State>(commentRepository.getReplies(action.comment).toObservable(), commentRepository.getComment(action.comment),
                BiFunction { t1, t2 -> RepliesContract.State.Replies(t2, t1) }).startWith(RepliesContract.State.Loading)
        is RepliesContract.Action.ViewUser -> stateless { mainNavigator.toUser(action.user) }
    }
}