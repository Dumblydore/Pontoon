package me.mauricee.pontoon.ext

import com.nytimes.android.external.store3.base.impl.Store
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Flowable
import io.reactivex.Observable
import me.mauricee.pontoon.model.DataModel

fun <T : Any, V : Any> StoreRoom<T, V>.getAsDataModel(id: V): DataModel<T> = DataModel(
        { getAndFetch(id) },
        { fetch(id).firstOrError() }
)

fun <T, V> StoreRoom<T, V>.getAndFetch(id: V): Observable<T> = get(id)
        .firstElement().toObservable().mergeWith(fetch(id)).distinctUntilChanged()
        .doOnError(::loge)

fun <T, V> Store<T, V>.getAndFetch(id: V): Flowable<T> = get(id)
        .mergeWith(fetch(id)).distinctUntilChanged()
        .doOnError(::loge)