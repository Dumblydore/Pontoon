package me.mauricee.pontoon.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.livedata.LiveEvent
import me.mauricee.pontoon.ext.referentialDistinctUntilChanged

abstract class EventViewModel<S : Any, A : EventTracker.Action, E : Any>(initialState: S, presenter: BasePresenter<S, *, A, E>) : ViewModel(), BaseContract.View<A> {
    private val _actions = PublishRelay.create<A>()
    override val actions: Observable<A>
        get() = _actions.hide()
    private val _state = MutableLiveData<S>()
    val state: LiveData<S>
        get() = _state.referentialDistinctUntilChanged()
    private val subs = CompositeDisposable()
    private val _events = LiveEvent<E>()
    val events: LiveData<E>
        get() = _events

    init {
        subs += presenter.events.subscribe(_events::postValue)
        subs += presenter.attachView(this, initialState).subscribe(::updateState)
    }

    fun sendAction(action: A) = _actions.accept(action)

    private fun updateState(state: S) = _state.postValue(state)

    override fun onCleared() {
        super.onCleared()
        subs.dispose()
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
