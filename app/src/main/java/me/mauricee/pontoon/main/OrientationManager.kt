package me.mauricee.pontoon.main

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class OrientationManager @Inject constructor(private val activity: AppCompatActivity) {

    var isFullscreen: Boolean = false
        set(value) {
            field = value
            activity.requestedOrientation = if (field) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
}