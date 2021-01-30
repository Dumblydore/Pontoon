package me.mauricee.pontoon.ui.main.creator

import io.reactivex.Observable
import me.mauricee.pontoon.common.StateBoundaryCallback
import me.mauricee.pontoon.model.creator.CreatorRepository
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.ui.BaseContract
import me.mauricee.pontoon.ui.ReduxPresenter
import me.mauricee.pontoon.ui.UiError
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.main.MainContract
import javax.inject.Inject

class CreatorPresenter @Inject constructor(private val args: CreatorContract.Args,
                                           private val creatorRepository: CreatorRepository,
                                           private val videoRepository: VideoRepository,
                                           private val mainNavigator: MainContract.Navigator) : ReduxPresenter<CreatorContract.State, CreatorContract.Reducer, CreatorContract.Action, CreatorContract.Event>() {

    override fun onViewAttached(view: BaseContract.View<CreatorContract.State, CreatorContract.Action>): Observable<CreatorContract.Reducer> {
        val (pages, states, refresh) = videoRepository.getVideos(false, false, args.creator)
        val creator = creatorRepository.getCreator(args.creator)
                .map<CreatorContract.Reducer>(CreatorContract.Reducer::DisplayCreator)
                .onErrorReturn { CreatorContract.Reducer.Error(CreatorContract.Errors.Network) }
        return Observable.merge(pages.map(CreatorContract.Reducer::DisplayVideos),
                states.map(::processState),
                creator, view.actions.flatMap { handleActions(refresh, it) })
    }

    override fun onReduce(state: CreatorContract.State, reducer: CreatorContract.Reducer): CreatorContract.State {
        return when (reducer) {
            CreatorContract.Reducer.Loading -> state.copy(screenState = UiState.Loading)
            is CreatorContract.Reducer.DisplayCreator -> state.copy(screenState = UiState.Success, creator = reducer.creator)
            is CreatorContract.Reducer.DisplayVideos -> state.copy(videos = reducer.videos)
            CreatorContract.Reducer.Fetching -> state.copy(pageState = UiState.Loading)
            CreatorContract.Reducer.Fetched -> state.copy(pageState = UiState.Success)
            is CreatorContract.Reducer.PageError -> state.copy(pageState = UiState.Failed(UiError(reducer.error?.msg)))
            is CreatorContract.Reducer.Error -> state.copy(screenState = UiState.Failed(UiError(reducer.error?.msg)))
        }
    }

    private fun handleActions(refresh: () -> Unit, action: CreatorContract.Action): Observable<CreatorContract.Reducer> {
        return when (action) {
            is CreatorContract.Action.Refresh -> noReduce(refresh)
            is CreatorContract.Action.PlayVideo -> noReduce { mainNavigator.playVideo(action.video.id) }
        }
    }

    private fun processState(state: StateBoundaryCallback.State): CreatorContract.Reducer = when (state) {
        StateBoundaryCallback.State.Loading -> CreatorContract.Reducer.Fetching
        StateBoundaryCallback.State.Error -> CreatorContract.Reducer.Fetching
        StateBoundaryCallback.State.Finished -> CreatorContract.Reducer.Error(CreatorContract.Errors.NoVideos)
        StateBoundaryCallback.State.Fetched -> CreatorContract.Reducer.Fetched
    }
}