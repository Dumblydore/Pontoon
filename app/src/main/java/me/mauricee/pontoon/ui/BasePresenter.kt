package me.mauricee.pontoon.ui

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.analytics.EventTracker

abstract class BasePresenter<S : EventTracker.State, in V : BaseContract.View<S, *>>(protected val eventTracker: EventTracker)
    : BaseContract.Presenter<V> {

    private lateinit var viewDisposable: Disposable

    final override fun attachView(view: V) {
        viewDisposable = onViewAttached(view).observeOn(AndroidSchedulers.mainThread())
                .doOnNext { eventTracker.trackState(it, view) }
                .doOnSubscribe { eventTracker.trackStart(view) }
                .doOnDispose { eventTracker.trackStop(view) }
                .subscribe(view::updateState)
    }

    final override fun detachView() {
        viewDisposable.dispose()
        onViewDetached()
    }

    inline fun stateless(crossinline action: () -> Unit): Observable<S> =
            Observable.defer { action(); Observable.empty<S>() }

    inline fun stateless(action: Completable): Observable<S> = action.andThen(Observable.empty())

    protected open fun onViewAttached(view: V): Observable<S> = Observable.empty()

    protected open fun onViewDetached() {}
}

abstract class ReduxPresenter<S : Any, R : Any, A : EventTracker.Action, E : Any> {

    @Volatile
    private lateinit var state: S
    private val _events: Relay<E> = PublishRelay.create()

    val events: Observable<E>
        get() = _events.hide()

    fun attachView(view: BaseContract.View<S, A>, initialState: S): Observable<S> {
        state = initialState
        return onViewAttached(view)
                .concatMapSingle { reduce(state, it) }
                .startWith(initialState)
                .doOnDispose { onDetach() }
    }

    fun sendEvent(event: E) {
        _events.accept(event)
    }

    protected open fun onViewAttached(view: BaseContract.View<S, A>): Observable<R> = Observable.empty()

    protected fun noReduce(action: () -> Unit): Observable<R> = noReduce(Completable.fromAction(action))

    protected fun noReduce(action: Completable): Observable<R> = action.andThen(Observable.empty())

    protected abstract fun onReduce(state: S, reducer: R): S

    protected open fun onDetach() {

    }

    private fun reduce(oldState: S, reducer: R): Single<S> = Single.fromCallable {
        val newState = onReduce(oldState, reducer)
        state = newState
        newState
    }
}