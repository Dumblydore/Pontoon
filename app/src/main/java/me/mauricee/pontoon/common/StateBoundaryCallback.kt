package me.mauricee.pontoon.common

import androidx.paging.PagedList
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

open class StateBoundaryCallback<T : Any> : PagedList.BoundaryCallback<T>() {

    internal val stateRelay = BehaviorRelay.create<State>()
    val state :Observable<State> = stateRelay

    enum class State {
        LOADING,
        ERROR,
        FETCHED,
        FINISHED
    }


}