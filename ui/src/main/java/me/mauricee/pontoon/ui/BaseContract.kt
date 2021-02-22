package me.mauricee.pontoon.ui

import io.reactivex.Flowable

interface BaseContract {

    interface View<A> {
        val actions: Flowable<A>
    }

    interface Presenter<A> {
        fun attachView(view: View<A>)

        fun detachView()
    }
}