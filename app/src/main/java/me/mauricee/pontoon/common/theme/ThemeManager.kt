package me.mauricee.pontoon.common.theme

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.model.preferences.Preferences
import javax.inject.Inject

//TODO consolidate Preferences & SharedPreferences?
@AppScope
class ThemeManager @Inject constructor(private val prefs: Preferences, private val preferences: SharedPreferences) {
    private val relay = PublishRelay.create<Style>()
    var baseTheme: BaseTheme
        set(value) {
            style = when (value) {
                BaseTheme.Light -> Style.Light(style.primary, style.accent)
                BaseTheme.Black -> Style.Black(style.primary, style.accent)
            }
        }
        get() = style.theme

    var accentColor: AccentColor
        set(value) {
            style = when (style.theme) {
                BaseTheme.Light -> Style.Light(style.primary, value)
                BaseTheme.Black -> Style.Black(style.primary, value)
            }
        }
        get() = style.accent

    var primaryColor: PrimaryColor
        set(value) {
            style = when (style.theme) {
                BaseTheme.Light -> Style.Light(value, style.accent)
                BaseTheme.Black -> Style.Black(value, style.accent)
            }
        }
        get() = style.primary

    var style: Style
        get() = convertToStyle(
                BaseTheme.valueOf(preferences.getString(ThemeKey, BaseTheme.Light.toString())!!),
                PrimaryColor.valueOf(preferences.getString(PrimaryColorKey, PrimaryColor.Default.toString())!!),
                AccentColor.valueOf(preferences.getString(AccentColorKey, AccentColor.Default.toString())!!)
        )
        set(value) {
            preferences.edit {
                putString(ThemeKey, value.theme.toString())
                putString(PrimaryColorKey, value.primary.toString())
                putString(AccentColorKey, value.accent.toString())
            }
        }

    private var delegate: AppCompatDelegate? = null

    fun init() {
        prefs.dayNightMode.map(DayNightBehavior::valueOf).subscribe(::setDayNightBehavior)
    }

    fun attach(activity: AppCompatActivity): Disposable {
        activity.setStyle(style)
        delegate = activity.delegate
        val subs = CompositeDisposable()
        subs += relay.subscribe {
            activity.setStyle(it)
            activity.recreate()
        }
        subs += prefs.amoledNightMode.subscribe(::setAmoledMode)
        return subs
    }

    private var mode = AppCompatDelegate.getDefaultNightMode()

    fun toggleNightMode() {
        mode = if (mode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        delegate?.apply { setLocalNightMode(mode) }
    }

    fun setDayNightBehavior(behavior: DayNightBehavior) = when (behavior) {
        DayNightBehavior.AlwaysDay -> AppCompatDelegate.MODE_NIGHT_NO
        DayNightBehavior.AlwaysNight -> AppCompatDelegate.MODE_NIGHT_YES
        DayNightBehavior.Automatic -> AppCompatDelegate.MODE_NIGHT_AUTO
    }.with {
        AppCompatDelegate.setDefaultNightMode(it)
        mode = it
    }

    fun setAmoledMode(isAmoledMode: Boolean) {
        baseTheme = if (isAmoledMode) BaseTheme.Black else BaseTheme.Light
        commit()
    }

    fun commit() {
        relay.accept(style)
    }

    private fun convertToStyle(base: BaseTheme, primary: PrimaryColor, accent: AccentColor): Style = when (base) {
        BaseTheme.Light -> Style.Light(primary, accent)
        BaseTheme.Black -> Style.Black(primary, accent)
    }

    companion object {
        const val ThemeKey = "settings_base"
        const val PrimaryColorKey = "settings_primary"
        const val AccentColorKey = "settings_accent"
    }

    enum class DayNightBehavior {
        AlwaysDay,
        AlwaysNight,
        Automatic
    }
}