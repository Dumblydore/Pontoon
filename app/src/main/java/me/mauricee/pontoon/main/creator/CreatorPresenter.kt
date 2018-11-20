package me.mauricee.pontoon.main.creator

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class CreatorPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                           private val userRepository: UserRepository,
                                           private val mainNavigator: MainContract.Navigator,
                                           eventTracker: EventTracker) :
        BasePresenter<CreatorContract.State, CreatorContract.View>(eventTracker), CreatorContract.Presenter {

    override fun onViewAttached(view: CreatorContract.View): Observable<CreatorContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(this::handleActions)

    private fun handleActions(action: CreatorContract.Action): Observable<CreatorContract.State> = when (action) {
        is CreatorContract.Action.Refresh -> getVideos(action.creator, action.clean)
        is CreatorContract.Action.PlayVideo -> stateless { mainNavigator.playVideo(action.video) }
    }.onErrorReturnItem(CreatorContract.State.Error())

    private fun getVideos(creator: String, clean: Boolean) = userRepository.getCreators(creator)
            .map { it.first() }
            .flatMap {
                val result = videoRepository.getVideos(it, refresh = clean)
                Observable.merge(result.videos.distinctUntilChanged()
                        .map<CreatorContract.State>(CreatorContract.State::DisplayVideos),
                        result.state.map(::processState))
                        .startWith(CreatorContract.State.DisplayCreator(it))
            }
            .startWith(CreatorContract.State.Loading)
            .onErrorReturnItem(CreatorContract.State.Error())

    private fun processState(state: StateBoundaryCallback.State): CreatorContract.State = when (state) {
        StateBoundaryCallback.State.LOADING -> CreatorContract.State.Loading
        StateBoundaryCallback.State.ERROR -> CreatorContract.State.Error()
        StateBoundaryCallback.State.FINISHED -> CreatorContract.State.Error(CreatorContract.State.Error.Type.NoVideos)
        StateBoundaryCallback.State.FETCHED -> CreatorContract.State.Loading
    }
}