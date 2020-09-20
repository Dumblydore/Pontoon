package me.mauricee.pontoon.main.details.replies

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.floatplane.CommentInteraction
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.main.details.DetailsContract
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import javax.inject.Inject

class RepliesPresenter @Inject constructor(private val commentRepository: CommentRepository,
                                           private val navigator: DetailsContract.Navigator,
                                           private val userRepository: UserRepository,
                                           private val mainNavigator: MainContract.Navigator,
                                           private val player: Player,
                                           eventTracker: EventTracker) : RepliesContract.Presenter, BasePresenter<RepliesContract.State, RepliesContract.View>(eventTracker) {

    override fun onViewAttached(view: RepliesContract.View): Observable<RepliesContract.State> = view.actions.flatMap(::handleActions)
            .mergeWith(userRepository.activeUser.map(RepliesContract.State::CurrentUser))
            .onErrorReturnItem(RepliesContract.State.Error())

    private fun handleActions(action: RepliesContract.Action): Observable<RepliesContract.State> = when (action) {
        is RepliesContract.Action.Like -> stateless(commentRepository.interact(action.comment.id, CommentInteraction.Type.Like)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Like))
        is RepliesContract.Action.Dislike -> stateless(commentRepository.interact(action.comment.id, CommentInteraction.Type.Dislike)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Dislike))
        is RepliesContract.Action.Clear -> stateless(commentRepository.interact(action.comment.id, null)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Cleared))
        is RepliesContract.Action.Reply -> stateless { navigator.comment(player.currentlyPlaying!!.video.id, action.parent.id) }
        is RepliesContract.Action.Parent -> commentRepository.getComment(action.comment).map<RepliesContract.State> { RepliesContract.State.Replies(it, it.replies) }.startWith(RepliesContract.State.Loading).onErrorReturnItem(RepliesContract.State.Error())
        is RepliesContract.Action.ViewUser -> stateless { mainNavigator.toUser(action.user.id) }
    }
}