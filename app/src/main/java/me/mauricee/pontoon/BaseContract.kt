package me.mauricee.pontoon

import io.reactivex.Observable

interface BaseContract {

    interface View<in S, A> : EventTracker.Page {
        val actions: Observable<A>
            get() = Observable.empty<A>()

        fun updateState(state: S)
    }

    interface Presenter<in V> {
        fun attachView(view: V)

        fun detachView()
    }
}