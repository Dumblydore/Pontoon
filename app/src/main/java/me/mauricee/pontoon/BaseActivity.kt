package me.mauricee.pontoon

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.crashlytics.android.Crashlytics
import dagger.android.support.DaggerAppCompatActivity
import io.fabric.sdk.android.Fabric
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.R
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), EventTracker.Page {

    @Inject
    lateinit var themeManager: ThemeManager
    @Inject
    lateinit var privacyManager: PrivacyManager

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

    override fun onStart() {
        super.onStart()
        if (privacyManager.hasUserBeenPrompted) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.analytics_prompt_title)
                    .setMessage(R.string.analytics_prompt_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.analytics_prompt_positive, () -> pri)
                    .setNegativeButton(R.string.analytics_prompt_negative, () -> pri)
                    .create().show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        themeSub.dispose()
    }
}