package me.mauricee.pontoon.common.theme

import android.app.Activity
import android.content.SharedPreferences
import androidx.core.content.edit
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.rx.preferences.watchString
import javax.inject.Inject

@AppScope
class ThemeManager @Inject constructor(private val preferences: SharedPreferences) {

    var style: Style
        get() = convertToStyle(
                BaseTheme.valueOf(preferences.getString(ThemeKey, BaseTheme.Black.toString())),
                PrimaryColor.valueOf(preferences.getString(PrimaryColorKey, PrimaryColor.Default.toString())),
                AccentColor.valueOf(preferences.getString(AccentColorKey, AccentColor.Default.toString()))
        )
        set(value) {
            preferences.edit {
                putString(ThemeKey, value.theme.toString())
                putString(PrimaryColorKey, value.primary.toString())
                putString(AccentColorKey, value.accent.toString())
            }
        }

    fun attach(activity: Activity): Disposable {
        activity.setStyle(style)
        return Observable.combineLatest(
                preferences.watchString(ThemeKey).map { BaseTheme.valueOf(it) },
                preferences.watchString(PrimaryColorKey).map { PrimaryColor.valueOf(it) },
                preferences.watchString(AccentColorKey).map { AccentColor.valueOf(it) },
                Function3<BaseTheme, PrimaryColor, AccentColor, Style>(this@ThemeManager::convertToStyle)
        ).skip(1).subscribe {
            activity.setStyle(it)
            activity.recreate()
        }
    }

    private fun convertToStyle(base: BaseTheme, primary: PrimaryColor, accent: AccentColor): Style =
            when (base) {
                BaseTheme.Light -> Style.Light(primary, accent)
                BaseTheme.Dark -> Style.Dark(primary, accent)
                BaseTheme.Black -> Style.Black(accent)
            }

    private fun watchValueOrGetDefault(key: String, default: String): Observable<String> {
        return preferences.watchString(key).defaultIfEmpty(default)
    }

    companion object {
        const val ThemeKey = "BaseTheme"
        const val PrimaryColorKey = "PrimaryColor"
        const val AccentColorKey = "AccentColor"

    }
}