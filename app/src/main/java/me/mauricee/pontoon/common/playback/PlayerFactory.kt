package me.mauricee.pontoon.common.playback

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import dagger.Reusable
import io.reactivex.Observable
import javax.inject.Inject

@Reusable
class PlayerFactory @Inject constructor(private val localPlayer: SimpleExoPlayer, private val castPlayer: CastPlayer) : CastPlayer.SessionAvailabilityListener {
    private val currentlyPlayingRelay: Relay<Player> = BehaviorRelay.createDefault<Player>(if (castPlayer.isCastSessionAvailable) castPlayer else localPlayer)

    val playback: Observable<Player>
        get() = currentlyPlayingRelay.hide()

    override fun onCastSessionAvailable() {
        currentlyPlayingRelay.accept(castPlayer)
    }

    override fun onCastSessionUnavailable() {
        currentlyPlayingRelay.accept(localPlayer)
    }

    fun release() {
        localPlayer.release()
        castPlayer.release()
    }

    init {
        castPlayer.setSessionAvailabilityListener(this)
    }
}