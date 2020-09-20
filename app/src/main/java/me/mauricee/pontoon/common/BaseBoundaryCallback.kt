package me.mauricee.pontoon.common

import androidx.paging.PagedList
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import me.mauricee.pontoon.ext.loge

abstract class BaseBoundaryCallback<T> : PagedList.BoundaryCallback<T>(), Disposable {
    val pagingState: Observable<PagingState>
        get() = _pagingState.hide().distinctUntilChanged()
    protected val isFetching: Boolean
        get() = _pagingState.value == PagingState.Fetching || _pagingState.value == PagingState.InitialFetch
    protected val subscriptions = CompositeDisposable()
    private val pagingSubscriptions = CompositeDisposable()
    private val _pagingState = BehaviorSubject.createDefault<PagingState>(PagingState.Empty)

    init {
        subscriptions += pagingSubscriptions
    }

    final override fun isDisposed(): Boolean = subscriptions.isDisposed

    final override fun dispose() = subscriptions.clear()

    final override fun onZeroItemsLoaded() {
        if (!isFetching) {
            pagingSubscriptions += noItemsLoaded().startWith(PagingState.InitialFetch)
                    .doOnError(this::loge)
                    .onErrorReturnItem(PagingState.Error)
                    .subscribe(_pagingState::onNext)
        }
    }

    final override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (!isFetching) {
            pagingSubscriptions += endItemLoaded(itemAtEnd).startWith(PagingState.Fetching)
                    .doOnError(this::loge)
                    .onErrorReturnItem(PagingState.Error)
                    .subscribe(_pagingState::onNext)
        }
    }

    final override fun onItemAtFrontLoaded(itemAtFront: T) {
        if (!isFetching) {
            pagingSubscriptions += frontItemLoaded(itemAtFront).startWith(PagingState.Fetching)
                    .doOnError(this::loge)
                    .onErrorReturnItem(PagingState.Error)
                    .subscribe(_pagingState::onNext)
        }
    }

    fun refresh() {
        pagingSubscriptions.clear()
        pagingSubscriptions += clearItems().subscribeOn(Schedulers.io()).subscribe { onZeroItemsLoaded() }
    }

    protected abstract fun clearItems(): Completable
    protected abstract fun noItemsLoaded(): Observable<PagingState>
    protected abstract fun frontItemLoaded(itemAtFront: T): Observable<PagingState>
    protected abstract fun endItemLoaded(itemAtEnd: T): Observable<PagingState>
}