package me.mauricee.pontoon

import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.analytics.DebugTracker
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.FirebaseTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.di.DaggerAppComponent
import okhttp3.OkHttpClient
import javax.inject.Inject

class Pontoon : DaggerApplication() {
    @Inject
    lateinit var client: OkHttpClient
    @Inject
    lateinit var fireBaseTracker: FirebaseTracker
    @Inject
    lateinit var debugTracker: DebugTracker
    @Inject
    lateinit var privacyManager: PrivacyManager

    lateinit var sub: Disposable
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        sub = privacyManager.isAnalyticsEnabled.subscribe {
            if (it) {
                EventTracker.trackers += fireBaseTracker
            } else EventTracker.trackers -= fireBaseTracker
        }
        if (BuildConfig.DEBUG) {
            EventTracker.trackers += debugTracker
            Stetho.initializeWithDefaults(this);
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().application(this).build()
                    .also { it.inject(this) }
}