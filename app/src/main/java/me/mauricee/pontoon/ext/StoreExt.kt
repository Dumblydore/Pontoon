package me.mauricee.pontoon.ext

import com.nytimes.android.external.store3.base.impl.Store
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Flowable
import io.reactivex.Observable

fun <T, V> StoreRoom<T, V>.getAndFetch(id: V): Observable<T> = get(id)
        .firstElement().toObservable().mergeWith(fetch(id)).distinctUntilChanged()
        .doOnError(::loge)

fun <T, V> Store<T, V>.getAndFetch(id: V): Flowable<T> = get(id)
        .mergeWith(fetch(id)).distinctUntilChanged()
        .doOnError(::loge)