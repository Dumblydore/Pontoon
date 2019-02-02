package me.mauricee.pontoon.common

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseListAdapter<Action, Model, VH : RecyclerView.ViewHolder> : ListAdapter<Model, VH>, Disposable {

    internal val relay = PublishRelay.create<Action>()
    val actions: Observable<Action>
        get() = relay

    internal val subscriptions = CompositeDisposable()

    constructor(diffCallback: DiffUtil.ItemCallback<Model>) : super(diffCallback)
    constructor(config: AsyncDifferConfig<Model>) : super(config)

    override fun isDisposed(): Boolean = subscriptions.isDisposed
    override fun dispose() = subscriptions.dispose()
}