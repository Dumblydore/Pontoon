package me.mauricee.pontoon

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.analytics.DebugTracker
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.FirebaseTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.di.AppComponent
import me.mauricee.pontoon.di.DaggerAppComponent
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.worker.LiveStreamWorker
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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
    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    lateinit var appComponent: AppComponent

    private val subs: CompositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        subs += privacyManager.isAnalyticsEnabled.subscribe {
            if (it) {
                EventTracker.trackers += fireBaseTracker
            } else EventTracker.trackers -= fireBaseTracker
        }
        subs += accountManagerHelper.watchForLogin.subscribe {
            WorkManager.getInstance().apply { pruneWork() }
                    .enqueueUniquePeriodicWork("LiveStream", ExistingPeriodicWorkPolicy.REPLACE,
                            PeriodicWorkRequestBuilder<LiveStreamWorker>(5, TimeUnit.MINUTES).build())
        }
        if (BuildConfig.DEBUG) {
            EventTracker.trackers += debugTracker
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().application(this).build()
                    .also {
                        it.inject(this)
                        appComponent = it
                    }
}