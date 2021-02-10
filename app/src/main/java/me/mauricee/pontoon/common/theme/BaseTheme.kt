package me.mauricee.pontoon.common.theme

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleRes
import me.mauricee.pontoon.R

enum class BaseTheme(@StyleRes val style: Int) {
    Light(R.style.AppTheme),
    Black(R.style.AppTheme_Amoled);

    fun theme(context: Context): Resources.Theme = context.resources.newTheme()
            .apply { applyStyle(style, true) }

    companion object {
        fun fromString(value: String) = when {
            value.equals("light", true) -> Light
            value.equals("dark", true) -> Light
            value.equals("black", true) -> Light
            else -> Light
        }
    }
}