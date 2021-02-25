package me.mauricee.pontoon.tv.browse

import androidx.paging.PagedList
import dagger.hilt.android.lifecycle.HiltViewModel
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.ui.BaseViewModel
import me.mauricee.pontoon.ui.UiState
import javax.inject.Inject

data class BrowseState(val state: UiState = UiState.Empty,
                       val background: String? = null,
                       val rows: List<BrowseRow> = emptyList())

data class BrowseRow(val id: String, val name: String, val videos: PagedList<Video>)

sealed class BrowseReducer {
    data class UpdateRowPage(val rows: Map<Creator, BrowseRow>) : BrowseReducer()
    data class UpdateBackground(val background: String) : BrowseReducer()
    object ClearBackground : BrowseReducer()
}

sealed class BrowseAction {
    data class VideoSelected(val video: Video) : BrowseAction()
    object ClearVideoSelected : BrowseAction()
    data class VideoClicked(val video: Video) : BrowseAction()
}

sealed class BrowseEvent {
    data class PlayVideo(val videoId: String) : BrowseEvent()
}

@HiltViewModel
class BrowseViewModel @Inject constructor(p: BrowsePresenter) : BaseViewModel<BrowseState, BrowseAction, BrowseEvent>(BrowseState(), p)