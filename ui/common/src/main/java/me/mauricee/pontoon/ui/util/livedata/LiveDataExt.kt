package me.mauricee.pontoon.ui.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

//fun <T : Any> LiveData<T?>.notNull(): LiveData<T> = NotNullLiveData(this)
//
//inline fun <T, R> LiveData<T>.map(crossinline action: ((T) -> R)): LiveData<R> = Transformations.map(this) { action(it) }
//
//inline fun <T, R> LiveData<T>.mapDistinct(crossinline action: ((T) -> R)): LiveData<R> = Transformations.distinctUntilChanged(Transformations.map(this) { action(it) })
//
//fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> = Transformations.distinctUntilChanged(this)

fun <T> LiveData<T>.referentialDistinctUntilChanged(): LiveData<T> = MediatorLiveData<T>().also { out ->
    var firstTime = true
    out.addSource(this) { currentValue ->
        val previousValue = out.value
        if (firstTime
                || previousValue == null && currentValue != null
                || previousValue != null && previousValue !== currentValue) {
            firstTime = false
            out.value = currentValue
        }
    }
}

private class NotNullLiveData<T : Any>(data: LiveData<T?>) : MediatorLiveData<T>() {
    init {
        addSource(data) { it?.let(::setValue) }
    }
}
