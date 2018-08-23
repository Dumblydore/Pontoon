package me.mauricee.pontoon.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import me.mauricee.pontoon.Pontoon
import me.mauricee.pontoon.analytics.EventTrackerModule

@AppScope
@Component(modules = [AppModule::class, EventTrackerModule::class, AndroidSupportInjectionModule::class])
interface AppComponent : AndroidInjector<DaggerApplication> {

    fun inject(application: Pontoon)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): AppComponent.Builder

        fun build(): AppComponent
    }
}