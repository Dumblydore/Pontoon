package me.mauricee.pontoon.main.details

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.domain.floatplane.CommentInteraction
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.comment.CommentRepository
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.rx.RxTuple
import javax.inject.Inject

class DetailsPresenter @Inject constructor(private val player: Player,
                                           private val userRepository: UserRepository,
                                           private val commentRepository: CommentRepository,
                                           private val videoRepository: VideoRepository,
                                           private val detailsNavigator: DetailsContract.Navigator,
                                           private val navigator: MainContract.Navigator,
                                           eventTracker: EventTracker) :
        DetailsContract.Presenter, BasePresenter<DetailsContract.State, DetailsContract.View>(eventTracker) {

    override fun onViewAttached(view: DetailsContract.View): Observable<DetailsContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(this::handleAction).startWith(DetailsContract.State.Loading)
                    .mergeWith(userRepository.activeUser.map(DetailsContract.State::CurrentUser))
                    .onErrorReturnItem(DetailsContract.State.Error())

    private fun handleAction(it: DetailsContract.Action): Observable<DetailsContract.State> = when (it) {
        is DetailsContract.Action.PlayVideo -> loadVideo(it.id)
        is DetailsContract.Action.PostComment -> stateless { detailsNavigator.comment(player.currentlyPlaying!!.video.id) }
        is DetailsContract.Action.Reply -> stateless { detailsNavigator.comment(player.currentlyPlaying!!.video.id, it.parent.id) }
        is DetailsContract.Action.ViewReplies -> stateless { detailsNavigator.displayReplies(it.comment.id) }
        is DetailsContract.Action.ViewUser -> stateless { navigator.toUser(it.user.id) }
        is DetailsContract.Action.ViewCreator -> stateless { player.currentlyPlaying?.video?.creator?.entity?.apply { navigator.toCreator(id, name) } }
        is DetailsContract.Action.Like -> stateless(commentRepository.interact(it.comment.id, CommentInteraction.Type.Like))
                .onErrorReturnItem(DetailsContract.State.Error(DetailsContract.ErrorType.Like))
        is DetailsContract.Action.Dislike -> stateless(commentRepository.interact(it.comment.id, CommentInteraction.Type.Dislike))
                .onErrorReturnItem(DetailsContract.State.Error(DetailsContract.ErrorType.Dislike))
    }.onErrorReturnItem(DetailsContract.State.Error())

    private fun loadVideo(video: String): Observable<DetailsContract.State> =
            Observable.merge(loadRelatedVideos(video), loadComments(video), loadVideoDetails(video)).doOnNext { logd("${it::class.simpleName}") }

    private fun loadVideoDetails(videoId: String): Observable<DetailsContract.State> = videoRepository.getVideo(videoId).map(DetailsContract.State::VideoInfo)

//            RxTuple.combineLatestAsTriple(videoRepository.getVideo(videoId),
//            videoRepository.getQualityOfVideo(videoId), videoRepository.addToWatchHistory(videoId).andThen(Observable.just(Unit))).flatMap {
//        val (video, quality) = it
    //TODO fix playback
//        Completable.fromAction { player.currentlyPlaying = Playback(video, quality) }.subscribeOn(AndroidSchedulers.mainThread())
//                .andThen(Observable.just(DetailsContract.State.VideoInfo(video)))
//        Observable.just(DetailsContract.State.VideoInfo(video))
//    }

    private fun loadRelatedVideos(video: String): Observable<DetailsContract.State> =
            videoRepository.getRelatedVideos(video).map(DetailsContract.State::RelatedVideos)

    //TODO add paging states
    private fun loadComments(video: String): Observable<DetailsContract.State> = Observable.defer {
        val comments = commentRepository.getComments(video)
        comments.pages.map(DetailsContract.State::Comments)//.firstElement().toObservable()
    }
}