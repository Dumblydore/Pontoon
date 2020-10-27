package me.mauricee.pontoon.ui.main.search

import io.reactivex.Observable
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.ui.main.MainContract
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.rx.RxTuple
import javax.inject.Inject

class SearchPresenter @Inject constructor(private val navigator: MainContract.Navigator,
                                          private val subscriptionRepository: SubscriptionRepository,
                                          private val videoRepository: VideoRepository, eventTracker: EventTracker) :
        BasePresenter<SearchContract.State, SearchContract.View>(eventTracker), SearchContract.Presenter {

    override fun onViewAttached(view: SearchContract.View): Observable<SearchContract.State> = RxTuple.combineLatestAsPair(subscriptionRepository.subscriptions,view.actions.doOnNext { eventTracker.trackAction(it, view) })
            .switchMap {
                val (subscribedCreators, action) = it
                handleActions(subscribedCreators,action)

            }.onErrorReturnItem(SearchContract.State.Error())


    private fun handleActions(creators: List<Creator>, action: SearchContract.Action): Observable<SearchContract.State> =
            when (action) {
                is SearchContract.Action.Query -> {
                    if (action.query.isEmpty()) Observable.just<SearchContract.State>(SearchContract.State.Error(SearchContract.State.Type.NoText))
                    else videoRepository.search(action.query, *creators.map { it.id }.toTypedArray()).let { result ->
                        Observable.merge(result.videos.map(SearchContract.State::Results), result.state.map { handleResultState(it, result.retry) })
                                .onErrorReturnItem(SearchContract.State.Error())
                    }
                }
                is SearchContract.Action.PlayVideo -> stateless { navigator.playVideo(action.video.id) }
            }

    private fun handleResultState(state: StateBoundaryCallback.State, retry: () -> Unit): SearchContract.State = when (state) {
        StateBoundaryCallback.State.Loading -> SearchContract.State.FetchingPage
        StateBoundaryCallback.State.Error -> SearchContract.State.FetchError(SearchContract.State.Type.Network, retry)
        StateBoundaryCallback.State.Fetched -> SearchContract.State.FinishFetching
        StateBoundaryCallback.State.Finished -> SearchContract.State.FetchError(SearchContract.State.Type.NoResults, retry)
    }
}