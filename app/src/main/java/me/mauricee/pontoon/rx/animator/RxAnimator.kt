package me.mauricee.pontoon.rx.animator

import android.animation.ValueAnimator
import io.reactivex.Observable

fun ValueAnimator.updates() : Observable<ValueAnimator> {
    return AnimatorObservable(this)
}