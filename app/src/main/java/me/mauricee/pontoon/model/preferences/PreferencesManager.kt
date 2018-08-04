package me.mauricee.pontoon.model.preferences

import android.content.SharedPreferences
import me.mauricee.pontoon.di.AppScope
import javax.inject.Inject

@AppScope
class PreferencesManager @Inject constructor(private val preferences: SharedPreferences) {

    companion object {
        public const val DefaultQualityWifiKey = ""
    }


}