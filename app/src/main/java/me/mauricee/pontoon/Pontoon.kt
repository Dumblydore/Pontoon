package me.mauricee.pontoon

import com.crashlytics.android.Crashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import me.mauricee.pontoon.analytics.CrashlyticsTracker
import me.mauricee.pontoon.analytics.DebugTracker
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.FirebaseTracker
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
    lateinit var crashlyticsTracker: CrashlyticsTracker

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        EventTracker.trackers += listOf(fireBaseTracker, crashlyticsTracker)
        if (BuildConfig.DEBUG)
            EventTracker.trackers += debugTracker
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().application(this).build()
                    .also { it.inject(this) }
}