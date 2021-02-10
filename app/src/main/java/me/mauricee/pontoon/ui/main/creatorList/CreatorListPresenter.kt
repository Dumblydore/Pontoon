package me.mauricee.pontoon.ui.main.creatorList

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.model.DataModel
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.creator.CreatorRepository
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.BasePresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

class CreatorListPresenter @Inject constructor(private val creatorRepository: CreatorRepository,
                                               private val subscriptionRepository: SubscriptionRepository) : BasePresenter<CreatorListContract.State, CreatorListContract.Reducer, CreatorListContract.Action, CreatorListContract.Event>() {

    override fun onViewAttached(view: BaseContract.View<CreatorListContract.Action>): Observable<CreatorListContract.Reducer> {
        val model = creatorRepository.allCreators
        return Observable.merge(loadSubscriptions(model), view.actions.flatMap { handleActions(model, it) })
    }

    override fun onReduce(state: CreatorListContract.State, reducer: CreatorListContract.Reducer): CreatorListContract.State {
        return when (reducer) {
            CreatorListContract.Reducer.Loading -> state.copy(uiState = UiState.Loading)
            CreatorListContract.Reducer.Refreshing -> state.copy(uiState = UiState.Refreshing)
            is CreatorListContract.Reducer.DisplayCreators -> state.copy(uiState = UiState.Success, creators = reducer.creators)
            is CreatorListContract.Reducer.DisplayError -> state.copy(uiState = UiState.Failed(UiError(message = reducer.error?.msg)))
        }
    }

    private fun loadSubscriptions(model: DataModel<List<Creator>>): Observable<CreatorListContract.Reducer> {
        return model.get().map<CreatorListContract.Reducer>(CreatorListContract.Reducer::DisplayCreators)
                .startWith(CreatorListContract.Reducer.Loading)
                .onErrorReturn(::handleError)
    }

    private fun handleActions(creators: DataModel<List<Creator>>, action: CreatorListContract.Action): Observable<CreatorListContract.Reducer> {
        return when (action) {
            is CreatorListContract.Action.CreatorSelected -> noReduce(subscriptionRepository.subscriptions.get().firstElement()
                    .filter { sub -> sub.any { action.creator.id == it.id } }
                    .map<CreatorListContract.Event> { CreatorListContract.Event.NavigateToCreator(action.creator) }
                    .switchIfEmpty(Single.just(CreatorListContract.Event.DisplayUnsubscribedPrompt(action.creator)))
                    .flatMapCompletable { Completable.fromAction { sendEvent(it) } })
            CreatorListContract.Action.Refresh -> creators.fetch()
                    .map<CreatorListContract.Reducer>(CreatorListContract.Reducer::DisplayCreators)
                    .toObservable().startWith(CreatorListContract.Reducer.Refreshing)
                    .onErrorReturn(::handleError)
        }
    }

    private fun handleError(e: Throwable): CreatorListContract.Reducer {
        return CreatorListContract.Reducer.DisplayError(CreatorListContract.Errors.Network)
    }
}