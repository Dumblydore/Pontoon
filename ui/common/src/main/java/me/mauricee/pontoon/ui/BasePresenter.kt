package me.mauricee.pontoon.ui

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.common.log.logd


abstract class BasePresenter<S : Any, R : Any, A : Any, E : Any> {

    @Volatile
    protected lateinit var state: S
        private set
    private val _events: Relay<E> = PublishRelay.create()

    val events: Observable<E>
        get() = _events.hide()

    fun attachView(view: BaseContract.View<A>, initialState: S): Observable<S> {
        state = initialState
        return onViewAttached(view)
                .doOnNext { logd("Reducer: ${it.javaClass.simpleName}") }
                .concatMapSingle { reduce(it) }
                .startWith(initialState)
                .doOnDispose { onDetach() }
    }

    fun sendEvent(event: E) {
        _events.accept(event)
    }

    protected open fun onViewAttached(view: BaseContract.View<A>): Observable<R> = Observable.empty()

    protected fun noReduce(action: () -> Unit): Observable<R> = noReduce(Completable.fromAction(action))

    protected fun noReduce(action: Completable): Observable<R> = action.andThen(Observable.empty())

    protected abstract fun onReduce(state: S, reducer: R): S

    protected open fun onDetach() {

    }

    private fun reduce(reducer: R): Single<S> = Single.fromCallable {
        state = onReduce(state, reducer)
        state
    }
}