package me.mauricee.pontoon.rx

import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseDisposable : Disposable {

    private val isDisposed = AtomicBoolean(false)

    final override fun dispose() {
        onDisposed()
        isDisposed.set(true)
    }

    final override fun isDisposed(): Boolean = isDisposed.get()

    protected abstract fun onDisposed()
 }