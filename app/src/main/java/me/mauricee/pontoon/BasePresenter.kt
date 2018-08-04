package me.mauricee.pontoon

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.ext.logd

abstract class BasePresenter<S : Any, V : BaseContract.View<S, *>> : BaseContract.Presenter<V> {

    internal val subs = CompositeDisposable()

    final override fun attachView(view: V) {
        subs += onViewAttached(view)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { logd("updating state: ${it::class.java.simpleName}") }
                .subscribe(view::updateState)
    }

    final override fun detachView() {
        subs.clear()
        onViewDetached()
    }

    inline fun stateless(crossinline action: () -> Unit): Observable<S> =
            Observable.defer { action(); Observable.empty<S>() }

    protected open fun onViewAttached(view: V): Observable<S> = Observable.empty()

    protected open fun onViewDetached() {}
}