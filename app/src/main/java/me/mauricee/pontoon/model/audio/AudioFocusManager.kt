package me.mauricee.pontoon.model.audio

import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.di.AppScope
import javax.inject.Inject

@AppScope
class AudioFocusManager @Inject constructor(private val audioManager: AudioManager) :
        AudioManager.OnAudioFocusChangeListener {

    private val focusRelay: Relay<FocusState> = PublishRelay.create()
    val focus: Observable<FocusState>
        get() = focusRelay.distinctUntilChanged()

    fun gain() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) gain_api23() else gain_compat()

    fun drop() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        drop_api23()
    } else {
        drop_compat()
    }

    private fun gain_compat() {
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gain_api23() {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build().let(audioManager::requestAudioFocus)
                .let(FocusState.Companion::valueOf)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drop_api23() {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_LOSS)
                .setAcceptsDelayedFocusGain(false)
                .build().let(audioManager::requestAudioFocus)
                .let(FocusState.Companion::valueOf)
    }

    private fun drop_compat() {
        audioManager.abandonAudioFocus(this)
    }

    override fun onAudioFocusChange(focus: Int) {
        focusRelay.accept(FocusState.valueOf(focus))
    }
}