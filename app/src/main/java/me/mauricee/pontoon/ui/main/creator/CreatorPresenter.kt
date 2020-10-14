package me.mauricee.pontoon.ui.main.creator

import io.reactivex.Observable
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.ui.main.MainContract
import me.mauricee.pontoon.model.creator.CreatorRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class CreatorPresenter @Inject constructor(private val creatorRepository: CreatorRepository,
                                           private val videoRepository: VideoRepository,
                                           private val mainNavigator: MainContract.Navigator,
                                           eventTracker: EventTracker) :
        BasePresenter<CreatorContract.State, CreatorContract.View>(eventTracker), CreatorContract.Presenter {

    override fun onViewAttached(view: CreatorContract.View): Observable<CreatorContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(this::handleActions)

    private fun handleActions(action: CreatorContract.Action): Observable<CreatorContract.State> = when (action) {
        is CreatorContract.Action.Refresh -> refresh(action.creator, action.clean)
        is CreatorContract.Action.PlayVideo -> stateless { mainNavigator.playVideo(action.video) }
    }.onErrorReturnItem(CreatorContract.State.Error())

    private fun refresh(creator: String, clean: Boolean): Observable<CreatorContract.State> = Observable.merge(
            creatorRepository.getCreator(creator).map(CreatorContract.State::DisplayCreator),
            getVideos(creator, clean)
    ).startWith(CreatorContract.State.Loading).onErrorReturnItem(CreatorContract.State.Error())

    private fun getVideos(creator: String, clean: Boolean): Observable<CreatorContract.State> {
        val result = videoRepository.getVideos(false, clean, creator)
        return Observable.merge(result.state.map(::processState), result.videos.map(CreatorContract.State::DisplayVideos))
    }

    private fun processState(state: StateBoundaryCallback.State): CreatorContract.State = when (state) {
        StateBoundaryCallback.State.Loading -> CreatorContract.State.Fetching
        StateBoundaryCallback.State.Error -> CreatorContract.State.Error()
        StateBoundaryCallback.State.Finished -> CreatorContract.State.Error(CreatorContract.State.Error.Type.NoVideos)
        StateBoundaryCallback.State.Fetched -> CreatorContract.State.Fetched
    }
}