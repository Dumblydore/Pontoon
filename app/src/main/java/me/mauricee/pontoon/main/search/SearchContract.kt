package me.mauricee.pontoon.main.search

import androidx.paging.PagedList
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.video.Video

interface SearchContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        class Query(val query: String) : Action()
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        object NoResults : State()
        object Error : State()
        class Results(val list: PagedList<Video>) : State()
    }
}