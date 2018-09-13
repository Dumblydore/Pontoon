package me.mauricee.pontoon.common

import androidx.annotation.CallSuper
import androidx.paging.PagedList
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

open class StateBoundaryCallback<T : Any> : PagedList.BoundaryCallback<T>() {

    internal val stateRelay = BehaviorRelay.create<State>()
    val state: Observable<State> = stateRelay
    private var lastCall: (() -> Unit)= {}

    fun retry() = lastCall()

    @CallSuper
    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()
        lastCall = this::onZeroItemsLoaded
    }

    @CallSuper
    override fun onItemAtEndLoaded(itemAtEnd: T) {
        lastCall = { onItemAtEndLoaded(itemAtEnd) }
    }

    @CallSuper
    override fun onItemAtFrontLoaded(itemAtFront: T) {
        lastCall = { onItemAtFrontLoaded(itemAtFront) }
    }

    enum class State {
        LOADING,
        ERROR,
        FETCHED,
        FINISHED
    }


}