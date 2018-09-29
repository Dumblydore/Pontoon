package me.mauricee.pontoon.model.audio

import android.media.AudioManager

enum class FocusState(private val code: Int) {
    Gained(AudioManager.AUDIOFOCUS_GAIN),
    Duck(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK),
    Transient(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT),
    Loss(AudioManager.AUDIOFOCUS_LOSS);

    companion object {
        fun valueOf(focusCode: Int): FocusState = when (focusCode) {
            AudioManager.AUDIOFOCUS_GAIN -> Gained
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Duck
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> Transient
            AudioManager.AUDIOFOCUS_LOSS -> Loss
            else -> throw RuntimeException("Invalid AudioFocus state")
        }
    }
}