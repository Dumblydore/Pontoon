package me.mauricee.pontoon.ui

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class Pontoon : Application() {
    @Inject
    lateinit var client: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AndroidThreeTen.init(this)
    }
    //    @Inject
//    lateinit var fireBaseTracker: FirebaseTracker
//
//    @Inject
//    lateinit var debugTracker: DebugTracker
//
//    @Inject
//    lateinit var privacyManager: PrivacyManager
//    lateinit var appComponent: AppComponent
//
//    lateinit var sub: Disposable
//    override fun onCreate() {
//        super.onCreate()
//        FirebaseApp.initializeApp(this)
//        AndroidThreeTen.init(this)
//        sub = privacyManager.isAnalyticsEnabled.subscribe {
//            if (it) {
//                EventTracker.trackers += fireBaseTracker
//            } else EventTracker.trackers -= fireBaseTracker
//        }
//        if (BuildConfig.DEBUG) {
//            EventTracker.trackers += debugTracker
//        }
//        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(this).getInt(ThemeManager.DayNightModeKey, AppCompatDelegate.MODE_NIGHT_AUTO_TIME))
//    }
//
//    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
//            DaggerAppComponent.builder().application(this).build()
//                    .also {
//                        it.inject(this)
//                        appComponent = it
//                    }
}