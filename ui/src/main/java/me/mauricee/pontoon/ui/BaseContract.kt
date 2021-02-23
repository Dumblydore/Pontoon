package me.mauricee.pontoon.ui

import io.reactivex.Flowable
import io.reactivex.Observable

interface BaseContract {

    interface View<A> {
        val actions: Observable<A>
    }

    interface Presenter<A> {
        fun attachView(view: View<A>)

        fun detachView()
    }
}