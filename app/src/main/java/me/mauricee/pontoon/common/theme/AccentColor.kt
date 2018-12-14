package me.mauricee.pontoon.common.theme

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.DiffUtil
import me.mauricee.pontoon.R

enum class AccentColor(@StyleRes val style: Int) {
    Default(R.style.AccentDefault),
    Red(R.style.AccentRed),
    Pink(R.style.AccentPink),
    Purple(R.style.AccentPurple),
    DeepPurple(R.style.AccentDeepPurple),
    Indigo(R.style.AccentIndigo),
    Blue(R.style.AccentBlue),
    LightBlue(R.style.AccentLightBlue),
    Cyan(R.style.AccentCyan),
    Teal(R.style.AccentTeal),
    Green(R.style.AccentGreen),
    LightGreen(R.style.AccentLightGreen),
    Lime(R.style.AccentLime),
    Yellow(R.style.AccentYellow),
    Amber(R.style.AccentAmber),
    Orange(R.style.AccentOrange),
    DeepOrange(R.style.AccentDeepOrange);

    fun theme(context: Context): Resources.Theme = context.resources.newTheme()
            .apply { applyStyle(style, true) }

    override fun toString(): String = when (this) {
        Default -> "Default"
        Red -> "Red"
        Pink -> "Pink"
        Purple -> "Purple"
        DeepPurple -> "DeepPurple"
        Indigo -> "Indigo"
        Blue -> "Blue"
        LightBlue -> "LightBlue"
        Cyan -> "Cyan"
        Teal -> "Teal"
        Green -> "Green"
        LightGreen -> "LightGreen"
        Lime -> "Lime"
        Yellow -> "Yellow"
        Amber -> "Amber"
        Orange -> "Orange"
        DeepOrange -> "DeepOrange"
    }

    companion object {
        val ItemCallback = object : DiffUtil.ItemCallback<AccentColor>() {
            override fun areItemsTheSame(oldItem: AccentColor, newItem: AccentColor): Boolean = oldItem.ordinal == newItem.ordinal

            override fun areContentsTheSame(oldItem: AccentColor, newItem: AccentColor): Boolean = newItem == oldItem
        }

        fun valueOf(key: String): AccentColor = when (key) {
            "Default" -> Default
            "Red" -> Red
            "Pink" -> Pink
            "Purple" -> Purple
            "DeepPurple" -> DeepPurple
            "Indigo" -> Indigo
            "Blue" -> Blue
            "LightBlue" -> LightBlue
            "Cyan" -> Cyan
            "Teal" -> Teal
            "Green" -> Green
            "LightGreen" -> LightGreen
            "Lime" -> Lime
            "Yellow" -> Yellow
            "Amber" -> Amber
            "Orange" -> Orange
            "DeepOrange" -> DeepOrange
            else -> Teal
        }
    }

}