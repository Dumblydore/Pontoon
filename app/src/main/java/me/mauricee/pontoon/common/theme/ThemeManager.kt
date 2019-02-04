package me.mauricee.pontoon.common.theme

import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.PublishRelay
import dagger.Reusable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.model.preferences.Preferences
import javax.inject.Inject

@Reusable
class ThemeManager @Inject constructor(private val prefs: Preferences,
                                       private val preferences: SharedPreferences,
                                       private val activity: AppCompatActivity) : LifecycleObserver {

    private val subs = CompositeDisposable()

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

    private var mode = AppCompatDelegate.getDefaultNightMode()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        subs += prefs.dayNightMode.map(DayNightBehavior::valueOf).subscribe(::setDayNightBehavior)
        activity.setStyle(style)
        subs += relay.subscribe {
            activity.setStyle(it)
            activity.recreate()
        }
        subs += prefs.amoledNightMode.subscribe(::setAmoledMode)
    }


    fun toggleNightMode() {
        mode = if (mode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        activity.delegate.setLocalNightMode(mode)
        if (BuildConfig.DEBUG)
            Toast.makeText(activity,"Switching to mode: $mode",Toast.LENGTH_LONG).show()
    }

    fun setDayNightBehavior(behavior: DayNightBehavior) = when (behavior) {
        DayNightBehavior.AlwaysDay -> AppCompatDelegate.MODE_NIGHT_NO
        DayNightBehavior.AlwaysNight -> AppCompatDelegate.MODE_NIGHT_YES
        DayNightBehavior.Automatic -> AppCompatDelegate.MODE_NIGHT_AUTO
    }.with {
        AppCompatDelegate.setDefaultNightMode(it)
        mode = it
        activity.delegate.applyDayNight()
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        subs.clear()
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