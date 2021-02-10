package me.mauricee.pontoon.ext

import android.animation.ValueAnimator
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.animation.ArgbEvaluatorCompat

fun Fragment.changingStatusBarColor() {
    val oldStatusBarColor = requireActivity().statusBarColor
    val animator = ValueAnimator().apply {
        duration = 250
        addUpdateListener { activity?.statusBarColor = it.animatedValue as Int }
    }
    requireActivity().lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            animator.cancel()
        }
    })
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            animator.apply {
                setIntValues(requireActivity().statusBarColor, oldStatusBarColor)
                setEvaluator(ArgbEvaluatorCompat())
            }.start()
        }
    })
}