package me.mauricee.pontoon.ui

import com.jakewharton.rx.replayingShare
import io.reactivex.Observable
import me.mauricee.pontoon.common.log.logd

abstract class ActionPresenter<S : Any, R : Any, A : Any, E : Any> : BasePresenter<S, R, A, E>() {
    override fun onViewAttached(view: BaseContract.View<A>): Observable<R> {
        val actions = view.actions.replayingShare()
        return Observable.merge(
                onViewAttached(),
                actions.filter(::concatMapFilter).concatMap(::concatMapAction),
                actions.filter(::flatMapFilter).flatMap(::flatMapAction),
                actions.filter(::switchMapFilter).switchMap(::switchMapAction)
        )
    }

    protected abstract fun onViewAttached(): Observable<R>

    protected open fun switchMapFilter(action: A): Boolean = false

    protected open fun switchMapAction(action: A): Observable<R> = noReduce {
        logd("Unhandled switchMapAction: ${action::class.java.simpleName}")
    }

    protected open fun concatMapFilter(action: A): Boolean = false

    protected open fun concatMapAction(action: A): Observable<R> = noReduce {
        logd("Unhandled concatMapAction: ${action::class.java.simpleName}")
    }

    protected open fun flatMapFilter(action: A): Boolean = !switchMapFilter(action) || !concatMapFilter(action)

    protected open fun flatMapAction(action: A): Observable<R> = noReduce {
        logd("Unhandled flatMapAction: ${action::class.java.simpleName}")
    }
}