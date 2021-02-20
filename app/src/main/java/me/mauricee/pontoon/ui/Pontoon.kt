package me.mauricee.pontoon.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.analytics.DebugTracker
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.FirebaseTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.common.theme.ThemeManager
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class Pontoon : Application() {
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
        FirebaseApp.initializeApp(this)
        AndroidThreeTen.init(this)
        sub = privacyManager.isAnalyticsEnabledChanges().subscribe {
            if (it) {
                EventTracker.trackers += fireBaseTracker
            } else EventTracker.trackers -= fireBaseTracker
        }
        if (BuildConfig.DEBUG) {
            EventTracker.trackers += debugTracker
        }
        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(this).getInt(ThemeManager.DayNightModeKey, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
    }
}