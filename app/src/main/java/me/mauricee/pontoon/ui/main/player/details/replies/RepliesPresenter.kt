package me.mauricee.pontoon.ui.main.player.details.replies

import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.main.MainContract
import javax.inject.Inject

class RepliesPresenter @Inject constructor(private val commentRepository: CommentRepository,
//                                           private val navigator: DetailsContract.Navigator,
                                           private val userRepository: UserRepository,
                                           private val mainNavigator: MainContract.Navigator,
                                           private val player: Player,
                                           eventTracker: EventTracker) : RepliesContract.Presenter, BasePresenter<RepliesContract.State, RepliesContract.View>(eventTracker) {

    override fun onViewAttached(view: RepliesContract.View): Observable<RepliesContract.State> = view.actions.flatMap(::handleActions)
            .mergeWith(userRepository.activeUser.map(RepliesContract.State::CurrentUser))
            .onErrorReturnItem(RepliesContract.State.Error())

    private fun handleActions(action: RepliesContract.Action): Observable<RepliesContract.State> = when (action) {
        is RepliesContract.Action.Like -> stateless(commentRepository.like(action.comment.id)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Like))
        is RepliesContract.Action.Dislike -> stateless(commentRepository.dislike(action.comment.id)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Dislike))
        is RepliesContract.Action.Clear -> stateless(commentRepository.clear(action.comment.id)).onErrorReturnItem(RepliesContract.State.Error(RepliesContract.ErrorType.Cleared))
        is RepliesContract.Action.Reply -> stateless {/* navigator.comment(player.currentlyPlaying!!.video.id, action.parent.id) */}
        is RepliesContract.Action.Parent -> commentRepository.getComment(action.comment).map<RepliesContract.State> { RepliesContract.State.Replies(it, it.replies) }.startWith(RepliesContract.State.Loading).onErrorReturnItem(RepliesContract.State.Error())
        is RepliesContract.Action.ViewUser -> stateless { /*mainNavigator.toUser(action.user.id) */}
    }
}