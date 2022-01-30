package me.mauricee.pontoon.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

fun <T : Any> LiveData<T?>.notNull(): LiveData<T> = NotNullLiveData(this)

inline fun <T, R> LiveData<T>.map(crossinline action: ((T) -> R)): LiveData<R> = Transformations.map(this) { action(it) }

inline fun <T, R> LiveData<T>.mapDistinct(crossinline action: ((T) -> R)): LiveData<R> = Transformations.distinctUntilChanged(Transformations.map(this) { action(it) })

private class NotNullLiveData<T : Any>(data: LiveData<T?>) : MediatorLiveData<T>() {
    init {
        addSource(data) { it?.let(::setValue) }
    }
}
