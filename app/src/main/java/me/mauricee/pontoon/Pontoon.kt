package me.mauricee.pontoon

import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
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

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        EventTracker.trackers += fireBaseTracker
        if (BuildConfig.DEBUG)
            EventTracker.trackers += debugTracker
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().application(this).build()
                    .also { it.inject(this) }
}