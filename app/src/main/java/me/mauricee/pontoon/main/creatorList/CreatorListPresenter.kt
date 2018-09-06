package me.mauricee.pontoon.main.creatorList

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.VideoRepository
import javax.inject.Inject

class CreatorListPresenter @Inject constructor(private val userRepository: UserRepository,
                                               private val videoRepository: VideoRepository,
                                               private val mainNavigator: MainContract.Navigator,
                                               eventTracker: EventTracker) :
        BasePresenter<CreatorListContract.State, CreatorListContract.View>(eventTracker), CreatorListContract.Presenter {

    override fun onViewAttached(view: CreatorListContract.View): Observable<CreatorListContract.State> = Observable.combineLatest<List<UserRepository.Creator>,
            CreatorListContract.Action, Pair<List<UserRepository.Creator>,
            CreatorListContract.Action>>(videoRepository.subscriptions, view.actions,
            BiFunction { t1, t2 -> Pair(t1, t2) })
            .flatMap { handleActions(it.first, it.second) }
            .startWith(getCreators())


    private fun handleActions(subscriptions: List<UserRepository.Creator>, action: CreatorListContract.Action) =
            when (action) {
                is CreatorListContract.Action.Creator -> checkForSubscription(subscriptions, action)
            }

    private fun checkForSubscription(subscriptions: List<UserRepository.Creator>, action: CreatorListContract.Action.Creator): Observable<CreatorListContract.State> {
        return if (subscriptions.contains(action.creator))
            stateless { mainNavigator.toCreator(action.creator) }
        else
            CreatorListContract.State.Error(CreatorListContract.State.Error.Type.Unsubscribed).toObservable()
    }

    private fun getCreators() = userRepository.getAllCreators()
            .map<CreatorListContract.State>(CreatorListContract.State::DisplayCreators)
            .doOnError { logd("Error!", it) }
            .onErrorReturnItem(CreatorListContract.State.Error())
}