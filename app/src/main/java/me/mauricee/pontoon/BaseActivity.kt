package me.mauricee.pontoon

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import dagger.android.support.DaggerAppCompatActivity
import io.fabric.sdk.android.Fabric
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.theme.ThemeManager
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), EventTracker.Page {

    @Inject
    lateinit var themeManager: ThemeManager

    private lateinit var themeSub: Disposable
    internal val subscriptions = CompositeDisposable()
    internal open val tag: String
        get() = this::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        themeSub = themeManager.attach(this)
    }

    override fun onStop() {
        super.onStop()
        subscriptions.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        themeSub.dispose()
    }
}