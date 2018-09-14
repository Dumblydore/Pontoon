package me.mauricee.pontoon.model.preferences

import android.content.SharedPreferences
import javax.inject.Inject


class Preferences @Inject constructor(private val sharedPreferences: SharedPreferences) {
    val displayUnwatchedVideos: Boolean
        get() = sharedPreferences.getBoolean(DisplayUnwatchedVideosKey, false)

    companion object {
        private const val BaseThemeKey = "settings_base"
        private const val PrimaryColorKey = "settings_primary"
        private const val AccentColorKey = "settings_accent"
        private const val DisplayUnwatchedVideosKey = "settings_hide"
        private const val PictureKey = "settings_picture"
        private const val QualityCellKey = "settings_quality_cell"
        private const val QualityWifiKey = "settings_quality_wifi"
    }
}