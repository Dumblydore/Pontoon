package me.mauricee.pontoon.model.video

import androidx.paging.PagedList
import io.reactivex.Observable
import me.mauricee.pontoon.common.StateBoundaryCallback

data class VideoResult(val videos: Observable<PagedList<Video>>, val state: Observable<StateBoundaryCallback.State>)