package me.mauricee.pontoon.common.theme

import android.app.Application
import android.content.Context
import androidx.annotation.ColorInt
import me.mauricee.pontoon.R

object NewThemeManager {
    private lateinit var context: Context
    private val baseAttrs = intArrayOf(
            R.attr.colorSurface,
            android.R.attr.windowBackground,
            android.R.attr.textColor,
            android.R.attr.textColorPrimary,
            android.R.attr.textColorSecondary,
            android.R.attr.textColorTertiary)


    fun init(application: Application) {
        context = application
    }
}

//enum class NewTheme(val primaryColor: ThemeColor,
//                    val accentThemeColor: ThemeColor,
//                    val buttonTheme: ButtonTheme,
//                    val textTheme: TextTheme,
//                    val surfaceTheme: SurfaceTheme,
//                    val windowThem: WindowTheme)

//data class ButtonTheme(val )
//data class TextTheme()
//data class SurfaceTheme()
//data class WindowTheme()

data class ThemeColor(@ColorInt val light: Int,
                      @ColorInt val default: Int,
                      @ColorInt val dark: Int)