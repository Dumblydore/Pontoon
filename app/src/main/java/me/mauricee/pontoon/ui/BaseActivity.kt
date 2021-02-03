package me.mauricee.pontoon.ui

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
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
//        themeManager.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        themeManager.clear()
        subscriptions.clear()
    }
}