package me.mauricee.pontoon.ui.main.creatorList

import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.DataModel
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.creator.CreatorRepository
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.ReduxPresenter
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.main.MainContract
import javax.inject.Inject

class CreatorListPresenter @Inject constructor(private val creatorRepository: CreatorRepository,
                                               private val subscriptionRepository: SubscriptionRepository,
                                               private val mainNavigator: MainContract.Navigator,
                                               eventTracker: EventTracker) : ReduxPresenter<CreatorListContract.State, CreatorListContract.Reducer, CreatorListContract.Action, CreatorListContract.Event>() {

    override fun onViewAttached(view: BaseContract.View<CreatorListContract.State, CreatorListContract.Action>): Observable<CreatorListContract.Reducer> {
        val model = creatorRepository.allCreators
        return Observable.merge(loadSubscriptions(model), view.actions.flatMap { handleActions(model, it) })
    }

    override fun onReduce(state: CreatorListContract.State, reducer: CreatorListContract.Reducer): CreatorListContract.State {
        return when (reducer) {
            CreatorListContract.Reducer.Loading -> state.copy(uiState = UiState.Loading)
            CreatorListContract.Reducer.Refreshing -> state.copy(uiState = UiState.Refreshing)
            is CreatorListContract.Reducer.DisplayCreators -> state.copy(uiState = UiState.Success, creators = reducer.creators)
            is CreatorListContract.Reducer.DisplayError -> TODO()
        }
    }

    private fun loadSubscriptions(model: DataModel<List<Creator>>): Observable<CreatorListContract.Reducer> {
        return model.get().map<CreatorListContract.Reducer>(CreatorListContract.Reducer::DisplayCreators)
                .startWith(CreatorListContract.Reducer.Loading)
    }

    private fun handleActions(creators: DataModel<List<Creator>>, action: CreatorListContract.Action): Observable<CreatorListContract.Reducer> {
        return when (action) {
            is CreatorListContract.Action.CreatorSelected -> noReduce { }
            CreatorListContract.Action.Refresh -> creators.fresh()
                    .map<CreatorListContract.Reducer>(CreatorListContract.Reducer::DisplayCreators)
                    .toObservable().startWith(CreatorListContract.Reducer.Refreshing)
        }
    }
}