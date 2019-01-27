package me.mauricee.pontoon.common.theme

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.crashlytics.android.Crashlytics
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.Disposable
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.ext.logd
import javax.inject.Inject

@AppScope
class ThemeManager @Inject constructor(private val preferences: SharedPreferences) {
    private val relay = PublishRelay.create<Style>()
    var baseTheme: BaseTheme
        set(value) {
            style = when (value) {
                BaseTheme.Light -> Style.Light(style.primary, style.accent)
                BaseTheme.Dark -> Style.Dark(style.primary, style.accent)
                BaseTheme.Black -> Style.Black(style.accent)
            }
        }
        get() = style.theme

    var accentColor: AccentColor
        set(value) {
            style = when (style.theme) {
                BaseTheme.Light -> Style.Light(style.primary, value)
                BaseTheme.Dark -> Style.Dark(style.primary, value)
                BaseTheme.Black -> Style.Black(style.accent)
            }
        }
        get() = style.accent

    var primaryColor: PrimaryColor
        set(value) {
            style = when (style.theme) {
                BaseTheme.Light -> Style.Light(value, style.accent)
                BaseTheme.Dark -> Style.Dark(value, style.accent)
                BaseTheme.Black -> Style.Dark(value, style.accent)
            }
        }
        get() = style.primary

    var style: Style
        get() = convertToStyle(
                BaseTheme.valueOf(preferences.getString(ThemeKey, BaseTheme.Light.toString())),
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

    private var delegate: AppCompatDelegate? = null
    fun attach(activity: AppCompatActivity): Disposable {
        activity.setStyle(style)
        delegate = activity.delegate
        return relay.subscribe {
            activity.setStyle(it)
            activity.recreate()
        }
    }

    private var mode = AppCompatDelegate.MODE_NIGHT_YES
        set(value) {
            delegate?.apply {
                setLocalNightMode(value)
            }
            field = value
        }

    fun toggleNightMode() {
        mode = if (mode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
    }

    fun commit() {
        relay.accept(style)
    }

    private fun convertToStyle(base: BaseTheme, primary: PrimaryColor, accent: AccentColor): Style = when (base) {
        BaseTheme.Light -> Style.Light(primary, accent)
        BaseTheme.Dark -> Style.Dark(primary, accent)
        BaseTheme.Black -> Style.Black(accent)
    }

    companion object {
        const val ThemeKey = "settings_base"
        const val PrimaryColorKey = "settings_primary"
        const val AccentColorKey = "settings_accent"

    }
}