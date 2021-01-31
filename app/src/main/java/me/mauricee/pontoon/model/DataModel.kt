package me.mauricee.pontoon.model

import androidx.paging.PagedList
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.common.BaseBoundaryCallback
import me.mauricee.pontoon.common.PagingState
import me.mauricee.pontoon.common.StateBoundaryCallback

class DataModel<T>(val get: () -> Observable<T>,
                   val fetch: () -> Single<T>)

data class PagedModel<T>(val pages: Observable<PagedList<T>> = Observable.empty(),
                         val state: Observable<StateBoundaryCallback.State>,
                         val refresh: () -> Unit,
                         val retry: () -> Unit)

data class NewPagedModel<T>(val pages: Observable<PagedList<T>> = Observable.empty(),
                         val state: Observable<PagingState>,
                         val refresh: () -> Unit)