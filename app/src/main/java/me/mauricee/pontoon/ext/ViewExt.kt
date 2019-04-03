package me.mauricee.pontoon.ext

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.transition.Transition

inline fun AppCompatActivity.loadFragment(isAddToBackStack: Boolean = false,
                                          transitionPairs: Map<String, View> = mapOf(),
                                          transaction: FragmentTransaction.() -> Unit) {
    val beginTransaction = supportFragmentManager.beginTransaction()
    beginTransaction.transaction()
    for ((name, view) in transitionPairs) {
        ViewCompat.setTransitionName(view, name)
        beginTransaction.addSharedElement(view, name)
    }

    if (isAddToBackStack) beginTransaction.addToBackStack(null)
    beginTransaction.commit()
}

fun Activity.isPortrait() = requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

fun Activity.setStatusBarColor(@ColorInt color: Int): Animator =
        ValueAnimator.ofArgb(window.statusBarColor, color).apply {
            addUpdateListener { window.statusBarColor = it.animatedValue as Int }
        }

fun Fragment.setStatusBarColor(@ColorInt color: Int): Animator = requireActivity().setStatusBarColor(color)

fun Activity.getDeviceWidth() = with(this) {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    displayMetrics.widthPixels
}

fun Activity.getDeviceHeight() = with(this) {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    displayMetrics.heightPixels
}

fun Fragment.toast(message: String, isLong: Boolean = false) {
    Toast.makeText(this.activity, message, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.removeFragmentByTag(tag: String): Boolean {
    return removeFragment(supportFragmentManager.findFragmentByTag(tag))
}

fun AppCompatActivity.removeFragmentByID(@IdRes containerID: Int): Boolean {
    return removeFragment(supportFragmentManager.findFragmentById(containerID))
}

fun AppCompatActivity.removeFragment(fragment: Fragment?): Boolean = fragment?.let {
    supportFragmentManager.beginTransaction().remove(fragment).commit();true
} ?: false

fun Activity.hasNotch() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
        window.decorView.rootWindowInsets?.displayCutout != null

var Activity.isFullscreen: Boolean
    get() = requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    set(value) {
        requestedOrientation = if (value) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View =
        LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)

inline fun Transition.doAfter(crossinline action: () -> Unit) {
    this.addListener(object : Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition) {
            action()
        }

        override fun onTransitionResume(transition: Transition) {

        }

        override fun onTransitionPause(transition: Transition) {

        }

        override fun onTransitionCancel(transition: Transition) {

        }

        override fun onTransitionStart(transition: Transition) {

        }

    })
}

inline fun ConstraintLayout.updateParams(constraintSet: ConstraintSet = ConstraintSet(), updates: ConstraintSet.() -> Unit) {
    constraintSet.clone(this)
    constraintSet.updates()
    constraintSet.applyTo(this)
}

fun View.getActivity(c: Context = context): Activity? = (c as? Activity)
        ?: (c as? ContextWrapper)?.baseContext?.let { getActivity(it) }

fun View.hide(doAfter: () -> Unit = {}) {
    animate()
            .setDuration(250)
            .alpha(0f)
            .withStartAction { alpha = 1f }
            .withEndAction {
                isVisible = false
                doAfter()
            }
            .start()
}

fun View.show(doAfter: () -> Unit = {}) {
    isVisible = true
    animate()
            .setDuration(250)
            .alpha(1f)
            .withStartAction {
                alpha = 0f
            }
            .withEndAction { doAfter() }
            .start()
}

val Fragment.supportActionBar: ActionBar?
    get() = (activity as? AppCompatActivity)?.supportActionBar