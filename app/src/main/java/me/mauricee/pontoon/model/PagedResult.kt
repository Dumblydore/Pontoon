package me.mauricee.pontoon.model

import androidx.paging.PagedList
import io.reactivex.Observable
import me.mauricee.pontoon.common.PagingState

data class PagedResult<T>(val pages: Observable<PagedList<T>>, val states: Observable<PagingState>, val retry: () -> Unit)