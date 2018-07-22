package me.mauricee.pontoon.main.history

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class HistoryPresenter @Inject constructor(private val videoRepository: VideoRepository,
                                           private val navigator: MainContract.Navigator) :
        HistoryContract.Presenter, BasePresenter<HistoryContract.State, HistoryContract.View>() {

    override fun onViewAttached(view: HistoryContract.View): Observable<HistoryContract.State> = videoRepository.watchHistory()
            .map(this::checkForEmptyList)
            .startWith(HistoryContract.State.Loading)
            .toObservable()
            .mergeWith(view.actions.flatMap(::handleAction))
            .onErrorReturnItem(HistoryContract.State.Error())

    private fun checkForEmptyList(it: List<Video>) = if (it.isEmpty())
        HistoryContract.State.Error(HistoryContract.State.Error.Type.NoVideos)
    else
        HistoryContract.State.DisplayVideos(it)

    private fun handleAction(action: HistoryContract.Action): Observable<HistoryContract.State> = stateless {
        when (action) {
            is HistoryContract.Action.PlayVideo -> navigator.playVideo(action.video)
        }
    }
}