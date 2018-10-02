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

    fun gain() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) gainApi23() else gainCompat()

    fun drop() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) dropApi23() else dropCompat()

    private fun gainCompat() {
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gainApi23() {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build().let(audioManager::requestAudioFocus)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dropApi23() {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_LOSS)
                .setAcceptsDelayedFocusGain(false)
                .build().let(audioManager::requestAudioFocus)
    }

    private fun dropCompat() {
        audioManager.abandonAudioFocus(this)
    }

    override fun onAudioFocusChange(focus: Int) {
        focusRelay.accept(FocusState.valueOf(focus))
    }
}