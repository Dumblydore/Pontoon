package me.mauricee.pontoon.tv.browse

import io.reactivex.Flowable
import io.reactivex.Observable
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.subscription.SubscriptionRepository
import me.mauricee.pontoon.repository.video.VideoRepository
import me.mauricee.pontoon.ui.ActionPresenter
import me.mauricee.pontoon.ui.UiState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BrowsePresenter @Inject constructor(private val subscriptionRepository: SubscriptionRepository,
                                          private val videoRepository: VideoRepository) : ActionPresenter<BrowseState, BrowseReducer, BrowseAction, BrowseEvent>() {

    override fun onViewAttached(): Observable<BrowseReducer> = refresh()

    override fun onReduce(state: BrowseState, reducer: BrowseReducer): BrowseState {
        return when (reducer) {
            is BrowseReducer.UpdateRowPage -> state.copy(state = UiState.Success, rows = reducer.rows.values.toList())
            BrowseReducer.ClearBackground -> state.copy(background = null)
            is BrowseReducer.UpdateBackground -> state.copy(background = reducer.background)
        }
    }

    private fun refresh() = subscriptionRepository.subscriptions.get().switchMap { creators ->
        val creatorRowMap = mutableMapOf<Creator, BrowseRow>()
        Flowable.fromIterable(creators).flatMap { creator ->
            videoRepository.getVideos(false, creator.id).pages.map<BrowseReducer> {
                creatorRowMap[creator] = BrowseRow(creator.id, creator.name, it)
                BrowseReducer.UpdateRowPage(creatorRowMap)
            }
        }
    }.toObservable()

    override fun switchMapFilter(action: BrowseAction): Boolean {
        return when (action) {
            is BrowseAction.VideoSelected, BrowseAction.ClearVideoSelected -> true
            else -> false
        }
    }

    override fun flatMapAction(action: BrowseAction): Observable<BrowseReducer> {
        return when (action) {
            is BrowseAction.VideoClicked -> noReduce { sendEvent(BrowseEvent.PlayVideo(action.video.id)) }
            else -> super.flatMapAction(action)
        }
    }

    override fun switchMapAction(action: BrowseAction): Observable<BrowseReducer> {
        return when (action) {
            is BrowseAction.VideoSelected -> Observable.timer(BackgroundTimer, TimeUnit.MILLISECONDS)
                    .map {
                        action.video.creator.coverImage?.let(BrowseReducer::UpdateBackground)
                                ?: BrowseReducer.ClearBackground
                    }
            BrowseAction.ClearVideoSelected -> Observable.just(BrowseReducer.ClearBackground)
            else -> super.switchMapAction(action)
        }
    }

    companion object {
        private const val BackgroundTimer = 300L
    }
}