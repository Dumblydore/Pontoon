package me.mauricee.pontoon.ext

import android.animation.ValueAnimator

inline fun ValueAnimator.updateAsInt(crossinline onUpdate: (Int) -> Unit): ValueAnimator {
    addUpdateListener { onUpdate(it.animatedValue as Int) }
    return this
}

inline fun ValueAnimator.updateAsFloat(crossinline onUpdate: (Float) -> Unit): ValueAnimator {
    addUpdateListener { onUpdate(it.animatedValue as Float) }
    return this
}