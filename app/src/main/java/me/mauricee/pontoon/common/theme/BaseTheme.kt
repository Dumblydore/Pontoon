package me.mauricee.pontoon.common.theme

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleRes
import me.mauricee.pontoon.R

enum class BaseTheme(@StyleRes val style: Int) {
    Light(R.style.AppTheme_Light),
    Dark(R.style.AppTheme_Dark),
    Black(R.style.AppTheme_Black);

    fun theme(context: Context): Resources.Theme = context.resources.newTheme()
            .apply { applyStyle(style, true) }
}