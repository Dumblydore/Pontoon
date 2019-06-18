package me.mauricee.pontoon.common

import androidx.annotation.CallSuper
import androidx.paging.PagedList
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

open class StateBoundaryCallback<T : Any> : PagedList.BoundaryCallback<T>() {

    internal val stateRelay = BehaviorRelay.create<State>()
    val state: Observable<State> = stateRelay
    private var lastCall: (() -> Unit) = ::onZeroItemsLoaded

    fun retry(): Unit = lastCall()

    @CallSuper
    override fun onZeroItemsLoaded() {
        if (stateRelay.value == State.Loading) return
        lastCall = ::onZeroItemsLoaded
    }

    @CallSuper
    override fun onItemAtEndLoaded(itemAtEnd: T) {
        if (stateRelay.value == State.Loading) return
        lastCall = { onItemAtEndLoaded(itemAtEnd) }
    }

    @CallSuper
    override fun onItemAtFrontLoaded(itemAtFront: T) {
        if (stateRelay.value == State.Loading) return
        lastCall = { onItemAtFrontLoaded(itemAtFront) }
    }

    enum class State {
        Loading,
        Error,
        Fetched,
        Finished
    }
}