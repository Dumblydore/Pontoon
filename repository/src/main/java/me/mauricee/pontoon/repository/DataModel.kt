package me.mauricee.pontoon.repository

import androidx.paging.PagedList
import io.reactivex.Flowable
import io.reactivex.Single
import me.mauricee.pontoon.repository.util.paging.PagingState

class DataModel<T : Any>(val get: () -> Flowable<T>,
                         val fetch: () -> Single<T>)

data class PagedModel<T : Any>(val pages: Flowable<PagedList<T>> = Flowable.empty(),
                               val state: Flowable<PagingState>,
                               val refresh: () -> Unit)