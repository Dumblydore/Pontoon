package me.mauricee.pontoon.common.theme

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleRes
import me.mauricee.pontoon.R

enum class PrimaryColor(@StyleRes val style: Int) {
    Default(R.style.PrimaryDefault),
    Red(R.style.PrimaryRed),
    Pink(R.style.PrimaryPink),
    Purple(R.style.PrimaryPurple),
    DeepPurple(R.style.PrimaryDeepPurple),
    Indigo(R.style.PrimaryIndigo),
    Blue(R.style.PrimaryBlue),
    LightBlue(R.style.PrimaryLightBlue),
    Cyan(R.style.PrimaryCyan),
    Teal(R.style.PrimaryTeal),
    Green(R.style.PrimaryGreen),
    LightGreen(R.style.PrimaryLightGreen),
    Lime(R.style.PrimaryLime),
    Yellow(R.style.PrimaryYellow),
    Amber(R.style.PrimaryAmber),
    Orange(R.style.PrimaryOrange),
    DeepOrange(R.style.PrimaryDeepOrange),
    Brown(R.style.PrimaryBrown),
    Grey(R.style.PrimaryGrey),
    BlueGrey(R.style.PrimaryBlueGrey),
    Black(R.style.PrimaryBlack);

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
        Brown -> "Brown"
        Grey -> "Grey"
        BlueGrey -> "BlueGrey"
        Black -> "Black"
    }

    companion object {
        fun valueOf(key: String): PrimaryColor = when (key) {
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
            "Brown" -> Brown
            "Grey" -> Grey
            "BlueGrey" -> BlueGrey
            "Black" -> Black
            else -> Default
        }
    }
}