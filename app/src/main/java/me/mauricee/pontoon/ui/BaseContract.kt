package me.mauricee.pontoon.ui

import io.reactivex.Observable
import me.mauricee.pontoon.analytics.EventTracker

interface BaseContract {

    interface View<A> : EventTracker.Page {
        val actions: Observable<A>
    }

    interface Presenter<A> {
        fun attachView(view: View<A>)

        fun detachView()
    }
}