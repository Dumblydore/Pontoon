package me.mauricee.pontoon.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.analytics.DebugTracker
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.FirebaseTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.di.AppComponent
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
    lateinit var appComponent: AppComponent

    lateinit var sub: Disposable
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AndroidThreeTen.init(this)
        sub = privacyManager.isAnalyticsEnabled.subscribe {
            if (it) {
                EventTracker.trackers += fireBaseTracker
            } else EventTracker.trackers -= fireBaseTracker
        }
        if (BuildConfig.DEBUG) {
            EventTracker.trackers += debugTracker
        }
        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(this).getInt(ThemeManager.DayNightModeKey, AppCompatDelegate.MODE_NIGHT_AUTO_TIME))
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().application(this).build()
                    .also {
                        it.inject(this)
                        appComponent = it
                    }
}