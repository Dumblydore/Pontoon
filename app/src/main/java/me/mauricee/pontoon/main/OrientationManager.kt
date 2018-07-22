package me.mauricee.pontoon.main

import android.content.Context
import android.content.pm.ActivityInfo
import javax.inject.Inject

@MainScope
class OrientationManager @Inject constructor(private val mainActivity: MainActivity) {

    var isFullscreen: Boolean = false
        set(value) {
            field = value
            mainActivity.requestedOrientation = if (field) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
}