package me.mauricee.pontoon.main.search

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
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

    private fun handleActions(creators: List<UserRepository.Creator>, action: SearchContract.Action): Observable<SearchContract.State> =
            when (action) {
                is SearchContract.Action.Query -> videoRepository.search(action.query, *creators.toTypedArray()).videos
                        .map<SearchContract.State>(SearchContract.State::Results)
                        .startWith(SearchContract.State.Loading)
                        .onErrorReturnItem(SearchContract.State.Error)
                is SearchContract.Action.PlayVideo -> stateless { navigator.playVideo(action.video) }
            }
}