package me.mauricee.pontoon.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.di.AppScope

@Module
class EventTrackerModule {

    @Provides
    @AppScope
    fun provideFirebase(context: Context): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
}