package me.mauricee.pontoon.common.theme

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleRes
import me.mauricee.pontoon.R

enum class BaseTheme(@StyleRes val style: Int) {
    Light(R.style.AppTheme),
    Dark(R.style.AppThemeDark),
    Black(R.style.AppThemeBlack);

    fun theme(context: Context): Resources.Theme = context.resources.newTheme()
            .apply { applyStyle(style, true) }
}