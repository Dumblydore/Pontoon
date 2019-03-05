package me.mauricee.pontoon

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.analytics.EventTracker

abstract class BasePresenter<S : EventTracker.State, in V : BaseContract.View<S, *>>(internal val eventTracker: EventTracker)
    : BaseContract.Presenter<V> {

    lateinit var viewDisposable: Disposable

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

    protected open fun onViewAttached(view: V): Observable<S> = Observable.empty()

    protected open fun onViewDetached() {}
}