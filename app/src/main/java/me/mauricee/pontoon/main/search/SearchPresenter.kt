package me.mauricee.pontoon.main.search

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.video.VideoRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchPresenter @Inject constructor(private val searchResultDiffCallback: SearchResultDiffCallback,
                                          private val navigator: MainContract.Navigator,
                                          private val videoRepository: VideoRepository,
                                          eventTracker: EventTracker) :
        BasePresenter<SearchContract.State, SearchContract.View>(eventTracker), SearchContract.Presenter {

    override fun onViewAttached(view: SearchContract.View): Observable<SearchContract.State> =
            view.actions.doOnNext { eventTracker.trackAction(it, view) }.flatMap(::handleActions)

    private fun handleActions(action: SearchContract.Action): Observable<SearchContract.State> =
            when (action) {
                is SearchContract.Action.Query -> videoRepository.search(action.query)
                        .buffer(500, TimeUnit.MILLISECONDS)
                        .map(searchResultDiffCallback::submit)
                        .startWith(SearchContract.State.Loading)
                        .onErrorReturnItem(SearchContract.State.Error)
                is SearchContract.Action.PlayVideo -> stateless { navigator.playVideo(action.video) }
            }
}