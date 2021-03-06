package me.mauricee.pontoon.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class EventTrackerModule {
    @Provides
    fun provideFirebase(context: Context): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
}