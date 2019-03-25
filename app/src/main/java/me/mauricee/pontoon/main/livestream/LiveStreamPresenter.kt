package me.mauricee.pontoon.main.livestream

import io.reactivex.Observable
import me.mauricee.pontoon.BasePresenter
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainContract
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.livestream.LiveStreamRepository
import javax.inject.Inject

class LiveStreamPresenter @Inject constructor(private val liveStreamRepository: LiveStreamRepository,
                                              private val player: Player,
                                              private val navigator: MainContract.Navigator,
                                              eventTracker: EventTracker) :
        LiveStreamContract.Presenter, BasePresenter<LiveStreamContract.State, LiveStreamContract.View>(eventTracker) {

    override fun onViewAttached(view: LiveStreamContract.View): Observable<LiveStreamContract.State> =
            view.actions.flatMap { handleActions(it) }.onErrorReturnItem(LiveStreamContract.State.Error())

    //TODO watch for oncomplete of player to know if it's offline.
    private fun handleActions(action: LiveStreamContract.Action): Observable<out LiveStreamContract.State> = when (action) {
        is LiveStreamContract.Action.ViewLiveStream -> liveStreamRepository.getLiveStreamOf(action.creatorId)
                .map<LiveStreamContract.State> { LiveStreamContract.State.IsOffline(it.liveStreamMetadata.offline) }.toObservable()
                .startWith(LiveStreamContract.State.Loading)
    }
}