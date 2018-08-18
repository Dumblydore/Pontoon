package me.mauricee.pontoon

import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity : DaggerAppCompatActivity(), EventTracker.Page {
    internal val subscriptions = CompositeDisposable()
    internal open val tag: String
        get() = this::class.java.simpleName

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }
}