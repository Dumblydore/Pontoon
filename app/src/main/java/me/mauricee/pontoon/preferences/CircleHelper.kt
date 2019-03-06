package me.mauricee.pontoon.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt
import me.mauricee.pontoon.R
import javax.inject.Inject

class CircleHelper @Inject constructor(private val context: Context) {
    fun buildCircle(@ColorInt color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        this.color = ColorStateList.valueOf(color)
    }

    fun buildSelectedCircle(@ColorInt color: Int, @ColorInt borderColor: Int): GradientDrawable =
            buildCircle(color).apply {
                setStroke(context.resources.getDimensionPixelSize(R.dimen.selection_stroke), borderColor)
            }
}

fun Int.lighten(factor: Float) = darken(1 + factor)
fun Int.darken(factor: Float): Int {
    val a = Color.alpha(this)
    val r = Math.round(Color.red(this) * factor)
    val g = Math.round(Color.green(this) * factor)
    val b = Math.round(Color.blue(this) * factor)
    return Color.argb(a,
            Math.min(r, 255),
            Math.min(g, 255),
            Math.min(b, 255))
}