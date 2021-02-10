package me.mauricee.pontoon.ext

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import androidx.annotation.ColorInt


var Activity.statusBarColor: Int
    @ColorInt get() = window.statusBarColor
    set(@ColorInt value) {
        window.statusBarColor = value
    }

fun Activity.animateStatusBarColor(@ColorInt colorTo: Int, @ColorInt colorFrom: Int = statusBarColor): Animator {
    return ValueAnimator.ofArgb(colorFrom, colorTo).apply {
        addUpdateListener { statusBarColor = it.animatedValue as Int }
    }
}