package me.mauricee.pontoon.tv


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PontoonTV : Application() {
    override fun onCreate() {
        super.onCreate()
//        FirebaseApp.initializeApp(this)
//        AndroidThreeTen.init(this)
//        sub = privacyManager.isAnalyticsEnabledChanges().subscribe {
//            if (it) {
//                EventTracker.trackers += fireBaseTracker
//            } else EventTracker.trackers -= fireBaseTracker
//        }
//        if (BuildConfig.DEBUG) {
//            EventTracker.trackers += debugTracker
//            Timber.plant(Timber.DebugTree())
//        }
//        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(this).getInt(ThemeManager.DayNightModeKey, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
    }
}