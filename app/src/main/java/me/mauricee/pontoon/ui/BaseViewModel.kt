package me.mauricee.pontoon.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ext.referentialDistinctUntilChanged

abstract class BaseViewModel<P : StatefulPresenter<S, A>, S, A : EventTracker.Action> : ViewModel, BaseContract.View<S, A> {
    private val _actions = PublishSubject.create<A>()
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

    override fun updateState(state: S) = _state.postValue(state)
}