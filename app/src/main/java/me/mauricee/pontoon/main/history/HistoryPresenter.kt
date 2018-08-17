package me.mauricee.pontoon.main.history

import androidx.paging.PagedList
import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class HistoryPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                           private val navigator: MainContract.Navigator,
                                           eventTracker: EventTracker) :
        HistoryContract.Presenter, BasePresenter<HistoryContract.State, HistoryContract.View>(eventTracker) {

    override fun onViewAttached(view: HistoryContract.View): Observable<HistoryContract.State> = videoRepository.watchHistory()
            .map(::checkForEmptyList) // Not sure how PagedList handles 'isEmpty'
//            .map<HistoryContract.State>(HistoryContract.State::DisplayVideos)
            .startWith(HistoryContract.State.Loading)
            .mergeWith(view.actions.flatMap(::handleAction))
            .onErrorReturnItem(HistoryContract.State.Error())

    private fun checkForEmptyList(it: PagedList<Video>) = if (it.isEmpty())
        HistoryContract.State.Error(HistoryContract.State.Error.Type.NoVideos)
    else
        HistoryContract.State.DisplayVideos(it)

    private fun handleAction(action: HistoryContract.Action): Observable<HistoryContract.State> = stateless {
        when (action) {
            is HistoryContract.Action.PlayVideo -> navigator.playVideo(action.video)
        }
    }
}