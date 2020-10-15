package me.mauricee.pontoon.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.referentialDistinctUntilChanged

abstract class BaseViewModel<S, A : EventTracker.Action> : ViewModel, BaseContract.View<S, A> {
    private val _actions = PublishRelay.create<A>()
    override val actions: Observable<A>
        get() = _actions.hide()
    protected open val _state = MutableLiveData<S>()
    val state: LiveData<S>
        get() = _state.referentialDistinctUntilChanged()
    protected val subs = CompositeDisposable()

    constructor() : super()

    constructor(initialState: S, presenter: StatefulPresenter<S, A>) : super() {
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
