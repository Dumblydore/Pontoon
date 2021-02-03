package me.mauricee.pontoon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.livedata.SingleLiveEvent
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.referentialDistinctUntilChanged

abstract class EventViewModel<S : Any, A : EventTracker.Action, E : Any>(initialState: S, presenter: ReduxPresenter<S, *, A, E>) : BaseViewModel<S, A>() {
    private val _events = SingleLiveEvent<E>()
    val events: LiveData<E>
        get() = _events

    init {
        subs += presenter.events.subscribe(_events::postValue)
        subs += presenter.attachView(this, initialState).subscribe(::updateState)
    }
}

abstract class BaseViewModel<S : Any, A : EventTracker.Action> : ViewModel, BaseContract.View<S, A> {
    private val _actions = PublishRelay.create<A>()
    override val actions: Observable<A>
        get() = _actions.hide()
    protected open val _state = MutableLiveData<S>()
    val state: LiveData<S>
        get() = _state.referentialDistinctUntilChanged()
    protected val subs = CompositeDisposable()

    constructor() : super()

    constructor(initialState: S, presenter: ReduxPresenter<S, *, A, *>) : super() {
        subs += presenter.attachView(this, initialState).subscribe(::updateState)
    }

    fun sendAction(action: A) = _actions.accept(action)

    fun <V> watchStateValue(diff: S.() -> V): LiveData<V> = _state.map(diff).referentialDistinctUntilChanged()

    override fun updateState(state: S) = _state.postValue(state)

    override fun onCleared() {
        super.onCleared()
        subs.dispose()
    }

    open class Factory<T>(private val creator: () -> T) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = creator() as T
    }

    open class SavedStateFactory<T>(owner: SavedStateRegistryOwner, arguments: Bundle?, private val creator: (SavedStateHandle) -> T) : AbstractSavedStateViewModelFactory(owner, arguments) {
        constructor(fragment: Fragment, creator: (SavedStateHandle) -> T) : this(fragment, fragment.arguments, creator)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T = creator(handle) as T
    }
}

inline fun <reified T : ViewModel> Fragment.assistedViewModel(
        crossinline viewModelProducer: (SavedStateHandle) -> T
) = viewModels<T> {
    object : AbstractSavedStateViewModelFactory(this, arguments) {
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle) =
                viewModelProducer(handle) as T
    }
}
