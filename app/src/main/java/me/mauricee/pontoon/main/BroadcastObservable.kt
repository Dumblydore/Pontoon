package me.mauricee.pontoon.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

class BroadcastObservable internal constructor(private val intentFilter: IntentFilter, private val context: Context) : Observable<BroadcastEvent>() {
    override fun subscribeActual(observer: Observer<in BroadcastEvent>) {
        observer.onSubscribe(Listener(intentFilter, observer, context))
    }

    inner class Listener(filter: IntentFilter, private val observer: Observer<in BroadcastEvent>, private val context: Context) : Disposable, BroadcastReceiver() {

        private val isDisposed = AtomicBoolean(false)
        override fun isDisposed(): Boolean = isDisposed.get()

        init {
            context.registerReceiver(this, filter)
        }

        override fun dispose() {
            context.unregisterReceiver(this)
            isDisposed.set(true)
        }

        override fun onReceive(context: Context, intent: Intent) = observer.onNext(BroadcastEvent(intent, context))

    }
}

data class BroadcastEvent(val intent: Intent, val context: Context)

fun Context.registerReceiver(intentFilter: IntentFilter): Observable<BroadcastEvent> = BroadcastObservable(intentFilter, this)