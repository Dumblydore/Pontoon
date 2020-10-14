package me.mauricee.pontoon.ui

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.analytics.EventTracker

abstract class BasePresenter<S : EventTracker.State, in V : BaseContract.View<S, *>>(internal val eventTracker: EventTracker)
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

abstract class StatefulPresenter<S, A : EventTracker.Action> {
    fun attachView(view: BaseContract.View<S, A>, initialState: S): Observable<S> {
        return onViewAttached(initialState).startWith(initialState).switchMap { newState ->
            view.actions.flatMap { action -> onAction(newState, action) }.startWith(newState)
        }
    }

    open fun onViewAttached(state: S): Observable<S> = Observable.empty()

    open fun onAction(state: S, action: A): Observable<S> = Observable.empty()

    internal fun stateless(action: () -> Unit): Observable<S> = Observable.defer { action(); Observable.empty() }

    internal fun stateless(action: Completable): Observable<S> = action.andThen(Observable.empty())
}