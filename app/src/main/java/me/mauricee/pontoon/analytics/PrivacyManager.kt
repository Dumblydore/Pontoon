package me.mauricee.pontoon.analytics

import android.content.SharedPreferences
import androidx.core.content.edit
import io.reactivex.Observable
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.rx.preferences.watchBoolean
import javax.inject.Inject

@AppScope
class PrivacyManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    val isAnalyticsEnabled: Observable<Boolean>
        get() = sharedPreferences.watchBoolean(AnalyticsEnabledKey, false)

    val hasUserBeenPrompted: Boolean
        get() = sharedPreferences.getBoolean(AnalyticsPromptKey, false)

    fun revokeAnalytics() = sharedPreferences.edit { putBoolean(AnalyticsEnabledKey, false) }

    fun allowAnalytics() = sharedPreferences.edit { putBoolean(AnalyticsEnabledKey, true) }

    companion object {
        private const val AnalyticsEnabledKey = "settings_analytics"
        private const val AnalyticsPromptKey = "AnalyticsPrompt"
    }
}