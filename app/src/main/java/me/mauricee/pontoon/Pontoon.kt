package me.mauricee.pontoon

import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import me.mauricee.pontoon.di.DaggerAppComponent
import okhttp3.OkHttpClient
import javax.inject.Inject

class Pontoon : DaggerApplication() {
    @Inject
    lateinit var client: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().application(this).build()
                    .also { it.inject(this) }
}