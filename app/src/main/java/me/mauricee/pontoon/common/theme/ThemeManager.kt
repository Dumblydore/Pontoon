package me.mauricee.pontoon.common.theme

import android.content.SharedPreferences
import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.palette.graphics.Palette
import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.rx.preferences.watchString
import javax.inject.Inject

@Reusable
class ThemeManager @Inject constructor(private val prefs: Preferences,
                                       private val preferences: SharedPreferences,
                                       private val activity: AppCompatActivity) : LifecycleObserver {
    private val subs = CompositeDisposable()

    val isInNightMode: Boolean
        get() = (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

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

    var style: Style = convertToStyle(
            BaseTheme.fromString(preferences.getString(ThemeKey, BaseTheme.Light.toString())!!),
            PrimaryColor.fromString(preferences.getString(PrimaryColorKey, PrimaryColor.Default.toString())!!),
            AccentColor.fromString(preferences.getString(AccentColorKey, AccentColor.Default.toString())!!))

    private val sylePreference
        get() = Observable.combineLatest<BaseTheme, PrimaryColor, AccentColor, Style>(
                preferences.watchString(ThemeKey, true).map(BaseTheme.Companion::fromString),
                preferences.watchString(PrimaryColorKey, true).map(PrimaryColor.Companion::fromString),
                preferences.watchString(AccentColorKey, true).map(AccentColor.Companion::fromString),
                Function3(this@ThemeManager::convertToStyle)).skip(1)

    private var mode
        get() = preferences.getInt(DayNightModeKey, AppCompatDelegate.MODE_NIGHT_NO)
        set(value) {
            preferences.edit(true) { putInt(DayNightModeKey, value) }
            activity.delegate.setLocalNightMode(value)
        }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        subs += prefs.dayNightMode.map(DayNightBehavior::valueOf).subscribe(::setDayNightBehavior)
        activity.setStyle(style)
        subs += sylePreference.subscribe {
            activity.setStyle(it)
            activity.recreate()
            style = it
        }
        subs += prefs.amoledNightMode.subscribe(::setAmoledMode)

    }

    fun getVibrantSwatch(palette: Palette) = (if (isInNightMode) palette.darkVibrantSwatch else palette.lightVibrantSwatch)
            ?: Palette.Swatch(activity.primaryColor, 1)

    fun getMutedSwatch(palette: Palette) = (if (isInNightMode) palette.darkMutedSwatch else palette.lightMutedSwatch)
            ?: Palette.Swatch(activity.primaryColor, 1)

    fun toggleNightMode() {
        mode = if (isInNightMode) AppCompatDelegate.MODE_NIGHT_NO
        else AppCompatDelegate.MODE_NIGHT_YES
        if (BuildConfig.DEBUG)
            Toast.makeText(activity, "Switching to mode: $mode", Toast.LENGTH_LONG).show()
    }

    private fun setDayNightBehavior(behavior: DayNightBehavior) = when (behavior) {
        DayNightBehavior.User -> mode
        DayNightBehavior.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        DayNightBehavior.Automatic -> AppCompatDelegate.MODE_NIGHT_AUTO
    }.with {
        mode = it
    }

    private fun setAmoledMode(isAmoledMode: Boolean) {
        baseTheme = if (isAmoledMode) BaseTheme.Black else BaseTheme.Light
        commit()
    }

    fun commit() {
        preferences.edit(true) {
            putString(ThemeKey, style.theme.toString())
            putString(PrimaryColorKey, style.primary.toString())
            putString(AccentColorKey, style.accent.toString())
        }
    }

    private fun convertToStyle(base: BaseTheme, primary: PrimaryColor, accent: AccentColor): Style = when (base) {
        BaseTheme.Light -> Style.Light(primary, accent)
        BaseTheme.Black -> Style.Black(primary, accent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        subs.clear()
    }

    companion object {
        const val ThemeKey = "settings_base"
        const val PrimaryColorKey = "settings_primary"
        const val AccentColorKey = "settings_accent"
        const val DayNightModeKey = "DayNightMode"
    }

    enum class DayNightBehavior {
        User,
        System,
        Automatic
    }
}