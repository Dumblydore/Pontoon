package me.mauricee.pontoon.common

import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseAdapter<Model, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>(), Disposable {

    internal val relay = PublishRelay.create<Model>()
    val actions: Observable<Model>
        get() = relay

    internal val subscriptions = CompositeDisposable()

    override fun isDisposed(): Boolean = false
    override fun dispose() = subscriptions.clear()
}