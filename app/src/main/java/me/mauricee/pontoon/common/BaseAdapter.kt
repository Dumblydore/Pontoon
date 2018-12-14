package me.mauricee.pontoon.common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseAdapter<M, A, VH : RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<M>)
    : ListAdapter<M, VH>(callback), Disposable {

    internal val relay = PublishRelay.create<A>()
    val actions: Observable<A>
        get() = relay.hide()

    internal val subscriptions = CompositeDisposable()

    override fun isDisposed(): Boolean = subscriptions.isDisposed
    override fun dispose() = subscriptions.dispose()
}

abstract class ModelAdapter<M, VH: RecyclerView.ViewHolder>(callback: DiffUtil.ItemCallback<M>) : BaseAdapter<M,M,VH>(callback)