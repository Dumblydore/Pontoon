package me.mauricee.pontoon.repository.util.store

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.store.rx2.freshSingle
import com.dropbox.store.rx2.observe
import io.reactivex.Flowable
import me.mauricee.pontoon.repository.DataModel

fun <T : Any, V : Any> Store<T, V>.getAsDataModel(id: T): DataModel<V> = DataModel(
        { observe(StoreRequest.cached(id, false)).map { it.requireData() } },
        { this.freshSingle(id) }
)

fun <T : Any, V : Any> Store<T, V>.getAndFetch(id: T): Flowable<V> = observe(StoreRequest.cached(id, true)).map { it.requireData() }