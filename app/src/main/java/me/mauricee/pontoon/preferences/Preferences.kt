package me.mauricee.pontoon.preferences

import android.content.SharedPreferences
import com.isupatches.wisefy.WiseFy
import io.reactivex.Observable
import me.mauricee.pontoon.playback.DefaultQuality
import me.mauricee.pontoon.rx.preferences.watchBoolean
import me.mauricee.pontoon.rx.preferences.watchString
import javax.inject.Inject


class Preferences @Inject constructor(private val sharedPreferences: SharedPreferences,
                                      private val wiseFy: WiseFy) {
    val displayUnwatchedVideos: Observable<Boolean>
        get() = sharedPreferences.watchBoolean(DisplayUnwatchedVideosKey)
    val defaultQualityLevel: String
        get() = (if (wiseFy.isDeviceConnectedToWifiNetwork()) sharedPreferences.getString(QualityWifiKey, DefaultQuality.p1080)
        else sharedPreferences.getString(QualityCellKey, DefaultQuality.p360)) ?: DefaultQuality.p360
    val pictureInPicture: PictureInPicture
        get() = PictureInPicture.valueOf(sharedPreferences.getString(PictureKey, PictureInPicture.Always.name)!!)
    val fullscreenOverNotch: Observable<Boolean>
        get() = sharedPreferences.watchBoolean(FullscreenOverNotchKey, false)
    val dayNightMode: Observable<String>
        get() = sharedPreferences.watchString(DayNightMode, true)
    val amoledNightMode: Observable<Boolean>
        get() = sharedPreferences.watchBoolean(AmoledNightMode, emitIfExists = false)

    companion object {
        private const val DayNightMode = "settings_night_mode_behavior"
        private const val AmoledNightMode = "settings_amoled_night_mode"
        private const val DisplayUnwatchedVideosKey = "settings_hide"
        private const val FullscreenOverNotchKey = "settings_notch"
        private const val PictureKey = "settings_picture"
        private const val QualityCellKey = "settings_quality_cell"
        private const val QualityWifiKey = "settings_quality_wifi"
    }

    enum class PictureInPicture {
        Always,
        OnlyWhenPlaying,
        Never
    }
}