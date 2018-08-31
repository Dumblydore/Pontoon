package me.mauricee.pontoon.common

import androidx.paging.PagedList
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

open class StateBoundaryCallback<T : Any> : PagedList.BoundaryCallback<T>() {

    internal val stateRelay = BehaviorRelay.createDefault(State.LOADING)
    val state :Observable<StateBoundaryCallback.State> = stateRelay
    internal var isLoading = false

    enum class State {
        LOADING,
        ERROR,
        FINISHED
    }


}