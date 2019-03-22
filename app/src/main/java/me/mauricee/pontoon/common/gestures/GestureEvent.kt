package me.mauricee.pontoon.common.gestures


import android.view.View

sealed class GestureEvent {
    data class Click(val view: View) : GestureEvent()
    data class Dismiss(val view: View) : GestureEvent()
    data class Scale(val percentage: Float) : GestureEvent()
    data class Swipe(val percentage: Float) : GestureEvent()
    data class Expand(val isExpanded: Boolean) : GestureEvent()
}