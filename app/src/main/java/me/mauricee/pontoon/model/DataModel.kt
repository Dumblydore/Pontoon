package me.mauricee.pontoon.model

import androidx.paging.PagedList
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.common.PagingState

class DataModel<T : Any>(val get: () -> Observable<T>,
                         val fetch: () -> Single<T>)

data class PagedModel<T : Any>(val pages: Observable<PagedList<T>> = Observable.empty(),
                               val state: Observable<PagingState>,
                               val refresh: () -> Unit)