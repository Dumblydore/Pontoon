package me.mauricee.pontoon.main.creatorList

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.model.creator.CreatorRepository
import me.mauricee.pontoon.model.subscription.SubscriptionRepository
import me.mauricee.pontoon.rx.RxTuple
import javax.inject.Inject

class CreatorListPresenter @Inject constructor(private val creatorRepository: CreatorRepository,
                                               private val subscriptionRepository: SubscriptionRepository,
                                               private val mainNavigator: MainContract.Navigator,
                                               eventTracker: EventTracker) :
        BasePresenter<CreatorListContract.State, CreatorListContract.View>(eventTracker), CreatorListContract.Presenter {

    override fun onViewAttached(view: CreatorListContract.View): Observable<CreatorListContract.State> = RxTuple.combineLatestAsPair(subscriptionRepository.subscriptions, view.actions).flatMap {
        val (subscribedCreators, action) = it
        handleActions(subscribedCreators, action)
    }.startWith(getCreators())

    private fun handleActions(subscriptions: List<Creator>, action: CreatorListContract.Action) =
            when (action) {
                is CreatorListContract.Action.CreatorSelected -> checkForSubscription(subscriptions, action)
            }

    private fun checkForSubscription(subscriptions: List<Creator>, action: CreatorListContract.Action.CreatorSelected): Observable<CreatorListContract.State> {
        return if (subscriptions.contains(action.creator))
            stateless { action.creator.entity.apply { mainNavigator.toCreator(name, id) } }
        else
            CreatorListContract.State.Error(CreatorListContract.State.Error.Type.Unsubscribed).toObservable()
    }

    private fun getCreators() = creatorRepository.allCreators
            .map<CreatorListContract.State>(CreatorListContract.State::DisplayCreators)
            .onErrorReturnItem(CreatorListContract.State.Error())
}