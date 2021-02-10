package me.mauricee.pontoon.ui.main.search

import androidx.annotation.StringRes
import androidx.paging.PagedList
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.ui.EventViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject


data class SearchState(val query: String = "",
                       val screenState: UiState = UiState.Empty,
                       val pageState: UiState = UiState.Empty,
                       val videos: PagedList<Video>? = null)

sealed class SearchReducer {
    object ClearVideos : SearchReducer()
    object Loading : SearchReducer()
    object FetchingPage : SearchReducer()
    object FinishFetching : SearchReducer()
    data class ScreenError(val error: SearchError) : SearchReducer()
    data class PageError(val error: SearchError) : SearchReducer()
    data class UpdateVideos(val videos: PagedList<Video>) : SearchReducer()
}

sealed class SearchAction : EventTracker.Action {
    data class Query(val query: String) : SearchAction()
}

typealias SearchEvent = Nothing

@HiltViewModel
class SearchViewModel @Inject constructor(p: SearchPresenter) : EventViewModel<SearchState, SearchAction, SearchEvent>(SearchState(), p)

enum class SearchError(@StringRes val msg: Int) {
    NoText(R.string.search_error_noText),
    NoResults(R.string.search_error_noResults),
    Network(R.string.search_error_network),
    General(R.string.search_error_general)
}
