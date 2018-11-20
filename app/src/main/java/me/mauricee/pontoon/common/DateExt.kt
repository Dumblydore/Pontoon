package me.mauricee.pontoon.common

import org.threeten.bp.Duration

fun Long.toDuration() = Duration.ofMillis(this).let {
    val seconds = it.seconds
    val absSeconds = Math.abs(seconds)
    String.format("%02d:%02d", absSeconds % 3600 / 60, absSeconds % 60)
}