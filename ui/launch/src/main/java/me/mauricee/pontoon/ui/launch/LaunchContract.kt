package me.mauricee.pontoon.ui.launch

sealed class LaunchEvent {
    data class ToLogin(val initializeWith2Fa: Boolean) : LaunchEvent()
    object ToSession : LaunchEvent()
}