package me.mauricee.pontoon.main.search

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class SearchPresenter @Inject constructor(private val navigator: MainContract.Navigator,
                                          private val videoRepository: VideoRepository, eventTracker: EventTracker) :
        BasePresenter<SearchContract.State, SearchContract.View>(eventTracker), SearchContract.Presenter {

    override fun onViewAttached(view: SearchContract.View): Observable<SearchContract.State> =
            Observable.combineLatest<List<UserRepository.Creator>, SearchContract.Action, Pair<List<UserRepository.Creator>, SearchContract.Action>>(
                    videoRepository.subscriptions, view.actions.doOnNext { eventTracker.trackAction(it, view) },
                    BiFunction { t1, t2 -> Pair(t1, t2) })
                    .flatMap { handleActions(it.first, it.second) }
                    .onErrorReturnItem(SearchContract.State.Error())

    private fun handleActions(creators: List<UserRepository.Creator>, action: SearchContract.Action): Observable<SearchContract.State> =
            when (action) {
                is SearchContract.Action.Query -> {
                    if (action.query.isEmpty()) Observable.just<SearchContract.State>(SearchContract.State.Error(SearchContract.State.Type.NoText))
                    else videoRepository.search("%${action.query}%", *creators.toTypedArray()).let { result ->
                        Observable.merge(result.videos.map(SearchContract.State::Results), result.state.map { handleResultState(it, result.retry) })
                                .onErrorReturnItem(SearchContract.State.Error())
                    }
                }
                is SearchContract.Action.PlayVideo -> stateless { navigator.playVideo(action.video) }
            }

    private fun handleResultState(state: StateBoundaryCallback.State, retry: () -> Unit): SearchContract.State = when (state) {
        StateBoundaryCallback.State.Loading -> SearchContract.State.FetchingPage
        StateBoundaryCallback.State.Error -> SearchContract.State.FetchError(SearchContract.State.Type.Network, retry)
        StateBoundaryCallback.State.Fetched -> SearchContract.State.FinishFetching
        StateBoundaryCallback.State.Finished -> SearchContract.State.FetchError(SearchContract.State.Type.NoResults, retry)
    }
}