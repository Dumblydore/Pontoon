package me.mauricee.pontoon.main.search

import androidx.recyclerview.widget.DiffUtil
import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.model.video.Video

interface SearchContract {

    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action {
        class Query(val query: String) : Action()
        class PlayVideo(val video: Video) : Action()
    }

    sealed class State {
        object Loading: State()
        object NoResults : State()
        object Error: State()
        class Results(val result: DiffUtil.DiffResult, val list: List<Video>) : State()
    }
}