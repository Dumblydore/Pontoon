package me.mauricee.pontoon.ui.main.creatorList

import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

interface CreatorListContract {

    sealed class Action : EventTracker.Action {
        object Refresh : Action()
        class CreatorSelected(val creator: Creator) : Action()
    }

    data class State(val uiState: UiState = UiState.Empty, val creators: List<Creator> = emptyList())

    sealed class Reducer {
        object Loading : Reducer()
        object Refreshing : Reducer()
        class DisplayCreators(val creators: List<Creator>) : Reducer()
        class DisplayError(val error: Errors? = null) : Reducer()
    }

    sealed class Event {
        class DisplayUnsubscribedPrompt(val creator: Creator) : Event()
        class NavigateToCreator(val creator: Creator) : Event()
    }

    @HiltViewModel
    class ViewModel @Inject constructor(p: CreatorListPresenter) : EventViewModel<State, Action, Event>(State(), p)

    enum class Errors(@StringRes val msg: Int) {
        Network(R.string.creators_list_error_noCreators)
    }
}