package me.mauricee.pontoon.rx.lazylayout

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import me.mauricee.pontoon.common.LazyLayout

class RetryObservable internal constructor(private val lazyLayout: LazyLayout) : Observable<Boolean>() {

    override fun subscribeActual(observer: Observer<in Boolean>) {
        lazyLayout.retryListener = Listener(lazyLayout, observer).also(observer::onSubscribe)
    }

    private class Listener(private val lazyLayout: LazyLayout,
                           private val observer: Observer<in Boolean>) : LazyLayout.RetryListener,
            MainThreadDisposable() {
        override fun onDispose() {
            lazyLayout.retryListener = null
        }

        override fun onRetry() {
            verifyMainThread()
            if (!isDisposed) {
                observer.onNext(true)
            }
        }

    }
}

fun LazyLayout.retries() = RetryObservable(this)