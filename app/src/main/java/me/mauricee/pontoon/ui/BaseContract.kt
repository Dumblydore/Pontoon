package me.mauricee.pontoon.ui

import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker

interface BaseContract {

    interface View<in S, A> : EventTracker.Page {
        val actions: Observable<A>
            get() = Observable.empty<A>()

        fun updateState(state: S)
    }

    interface Router<R> {
        fun routeTo(route: R)
    }

    interface Presenter<in V> {
        fun attachView(view: V)

        fun detachView()
    }
}