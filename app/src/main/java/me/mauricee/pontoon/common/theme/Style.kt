package me.mauricee.pontoon.common.theme


import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import me.mauricee.pontoon.R

sealed class Style(val theme: BaseTheme, val primary: PrimaryColor, val accent: AccentColor) {
    class Light(primary: PrimaryColor, accent: AccentColor) : Style(BaseTheme.Light, primary, accent)
    class Black(primary: PrimaryColor, accent: AccentColor) : Style(BaseTheme.Black, primary, accent)
}

val Resources.Theme.primaryColor: Int
    get() = TypedValue().apply { resolveAttribute(R.attr.colorPrimary, this, true) }
            .data

val Resources.Theme.primaryDarkColor: Int
    get() = TypedValue().apply { resolveAttribute(R.attr.colorPrimaryDark, this, true) }
            .data

val Resources.Theme.accentColor: Int
    get() = TypedValue().apply { resolveAttribute(R.attr.colorAccent, this, true) }
            .data

val Resources.Theme.primaryTextColor: Int
    get() = TypedValue().apply { resolveAttribute(R.attr.titleTextColor, this, true) }
            .data

val Resources.Theme.secondaryTextColor: Int
    get() = TypedValue().apply { resolveAttribute(R.attr.subtitleTextColor, this, true) }
            .data

val Resources.Theme.errorColor: Int
    get() = TypedValue().apply { resolveAttribute(R.attr.colorError, this, true) }
            .data

val Context.primaryColor: Int
    get() = theme.primaryColor

val Context.primaryDarkColor: Int
    get() = theme.primaryDarkColor

val Context.accentColor: Int
    get() = theme.accentColor

val Context.primaryTextColor: Int
    get() = theme.primaryTextColor

val Context.secondaryTextColor: Int
    get() = theme.secondaryTextColor

val Context.errorColor: Int
    get() = theme.errorColor

fun Activity.setStyle(style: Style) {
    this.theme.applyStyle(style.theme.style, true)
    this.theme.applyStyle(style.primary.style, true)
    this.theme.applyStyle(style.accent.style, true)
}