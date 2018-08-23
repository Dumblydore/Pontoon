package me.mauricee.pontoon

import android.os.Bundle
import android.os.PersistableBundle
import com.crashlytics.android.Crashlytics
import dagger.android.support.DaggerAppCompatActivity
import io.fabric.sdk.android.Fabric
import io.reactivex.disposables.CompositeDisposable


abstract class BaseActivity : DaggerAppCompatActivity(), EventTracker.Page {
    internal val subscriptions = CompositeDisposable()
    internal open val tag: String
        get() = this::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Fabric.with(this, Crashlytics())
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }
}