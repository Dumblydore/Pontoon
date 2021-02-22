package me.mauricee.pontoon.repository.util.paging

import androidx.paging.PagedList
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

abstract class BaseBoundaryCallback<T : Any> : PagedList.BoundaryCallback<T>(), Disposable {
    val pagingState: Observable<PagingState>
        get() = _pagingState.hide().distinctUntilChanged()
    private val isFetching: Boolean
        get() = _pagingState.value == PagingState.Fetching || _pagingState.value == PagingState.InitialFetch
    protected val subscriptions = CompositeDisposable()
    private val pagingSubscriptions = CompositeDisposable()
    private val _pagingState = BehaviorSubject.create<PagingState>()

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
                    .subscribeOn(Schedulers.io())
                    .subscribe(_pagingState::onNext)
        }
    }

    final override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (!isFetching) {
            pagingSubscriptions += endItemLoaded(itemAtEnd).startWith(PagingState.Fetching)
                    .doOnError(this::loge)
                    .onErrorReturnItem(PagingState.Error)
                    .subscribeOn(Schedulers.io())
                    .subscribe(_pagingState::onNext)
        }
    }

    final override fun onItemAtFrontLoaded(itemAtFront: T) {
        if (!isFetching) {
            pagingSubscriptions += frontItemLoaded(itemAtFront).startWith(PagingState.Fetching)
                    .doOnError(this::loge)
                    .onErrorReturnItem(PagingState.Error)
                    .subscribeOn(Schedulers.io())
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