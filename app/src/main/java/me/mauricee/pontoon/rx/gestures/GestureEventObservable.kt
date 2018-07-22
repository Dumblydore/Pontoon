package me.mauricee.pontoon.rx.gestures

import android.view.View
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import me.mauricee.pontoon.common.gestures.GestureEvents

class GestureEventObservable : Observable<Events>() {
    override fun subscribeActual(observer: Observer<in Events>?) {
//        observer.onSubscribe(Listener())
    }


    inner class Listener(private val observer: Observer<in Events>) : MainThreadDisposable(), GestureEvents {

        override fun onDispose() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onClick(view: View) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDismiss(view: View) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onScale(percentage: Float) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSwipe(percentage: Float) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onExpand(isExpanded: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

sealed class Events {
    class Click(val view: View) : Events()
    class Dismiss(val view: View) : Events()
    class Scale(val percentage: Float) : Events()
    class Swipe(val percentage: Float) : Events()
    class Expand(val isExpanded: Boolean) : Events()
}