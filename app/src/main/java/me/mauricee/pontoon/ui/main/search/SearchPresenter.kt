package me.mauricee.pontoon.ui.main.search

import io.reactivex.Flowable
import io.reactivex.Observable
import me.mauricee.pontoon.repository.util.paging.PagingState
import me.mauricee.pontoon.common.log.logd
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.subscription.SubscriptionRepository
import me.mauricee.pontoon.repository.video.VideoRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

class SearchPresenter @Inject constructor(private val subscriptionRepository: SubscriptionRepository,
                                          private val videoRepository: VideoRepository) : BasePresenter<SearchState, SearchReducer, SearchAction, SearchEvent>() {


    override fun onViewAttached(view: BaseContract.View<SearchAction>): Observable<SearchReducer> {
        return subscriptionRepository.subscriptions.get().toObservable().map { it.map(Creator::id) }
                .switchMap { creators -> view.actions.switchMap { handleActions(creators, it) } }
                .doOnNext { logd("Reducer: ${it::class.java.simpleName}") }
    }

    override fun onReduce(state: SearchState, reducer: SearchReducer): SearchState {
        return when (reducer) {
            SearchReducer.ClearVideos -> state.copy(videos = null)
            SearchReducer.Loading -> state.copy(screenState = UiState.Loading)
            SearchReducer.FetchingPage -> state.copy(pageState = UiState.Loading)
            SearchReducer.FinishFetching -> state.copy(pageState = UiState.Success)
            is SearchReducer.ScreenError -> state.copy(screenState = UiState.Failed(UiError(reducer.error.msg)))
            is SearchReducer.PageError -> state.copy(pageState = UiState.Failed(UiError(reducer.error.msg)))
            is SearchReducer.UpdateVideos -> state.copy(screenState = UiState.Success, videos = reducer.videos)
        }
    }

    private fun handleActions(creators: List<String>, action: SearchAction): Observable<SearchReducer> {
        return when (action) {
            is SearchAction.Query -> query(creators, action.query)
        }
    }

    private fun query(creators: List<String>, query: String): Observable<SearchReducer> {
        val (pages, state) = videoRepository.search(query, *creators.toTypedArray())
        return Flowable.merge(pages.map(SearchReducer::UpdateVideos), state.map(::handlePageState))
                .toObservable()

    }

    private fun handlePageState(state: PagingState): SearchReducer = when (state) {
        PagingState.InitialFetch -> SearchReducer.FetchingPage
        PagingState.Fetching -> SearchReducer.FetchingPage
        PagingState.Fetched -> SearchReducer.FinishFetching
        PagingState.Completed -> SearchReducer.FinishFetching
        PagingState.Empty -> SearchReducer.ScreenError(SearchError.NoResults)
        PagingState.Error -> SearchReducer.PageError(SearchError.General)
    }
}