package me.mauricee.pontoon

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import dagger.android.support.DaggerAppCompatActivity
import io.fabric.sdk.android.Fabric
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.theme.ThemeManager
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), EventTracker.Page {

    @Inject
    lateinit var themeManager: ThemeManager

    internal val subscriptions = CompositeDisposable()
    internal open val tag: String
        get() = this::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        subscriptions += themeManager.attach(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }
}