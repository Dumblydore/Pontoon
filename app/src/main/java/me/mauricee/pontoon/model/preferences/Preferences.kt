package me.mauricee.pontoon.model.preferences

import android.content.SharedPreferences
import com.isupatches.wisefy.WiseFy
import io.reactivex.Observable
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.rx.preferences.watchBoolean
import javax.inject.Inject


class Preferences @Inject constructor(private val sharedPreferences: SharedPreferences,
                                      private val wiseFy: WiseFy) {
    val displayUnwatchedVideos: Observable<Boolean>
        get() = sharedPreferences.watchBoolean(DisplayUnwatchedVideosKey)
    val defaultQualityLevel: Player.QualityLevel
        get() = (if (wiseFy.isDeviceConnectedToWifiNetwork()) sharedPreferences.getString(QualityWifiKey, "p1080")
        else sharedPreferences.getString(QualityCellKey, "p360")).let { Player.QualityLevel.valueOf(it) }
    val pictureInPicture: PictureInPicture
        get() = PictureInPicture.valueOf(sharedPreferences.getString(PictureKey, PictureInPicture.Always.name))

    companion object {
        private const val DisplayUnwatchedVideosKey = "settings_hide"
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