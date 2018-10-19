package me.mauricee.pontoon.rx.okhttp

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.Call
import okhttp3.Response

internal class CallObservable
(private val originalCall: Call) : Observable<Response>() {

    override fun subscribeActual(observer: Observer<in Response>) {
        // Since Call is a one-shot type, clone it for each new observer.
        val call = originalCall.clone()
        observer.onSubscribe(CallDisposable(call))

        var terminated = false
        try {
            val response = call.execute()
            if (!call.isCanceled) {
                observer.onNext(response)
            }
            if (!call.isCanceled) {
                terminated = true
                observer.onComplete()
            }
        } catch (t: Throwable) {
            Exceptions.throwIfFatal(t)
            if (terminated) {
                RxJavaPlugins.onError(t)
            } else if (!call.isCanceled) {
                try {
                    observer.onError(t)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }

            }
        }
    }

    private class CallDisposable internal constructor(private val call: Call) : Disposable {

        override fun dispose() = call.cancel()

        override fun isDisposed(): Boolean = call.isCanceled
    }
}