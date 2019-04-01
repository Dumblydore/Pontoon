package me.mauricee.pontoon.common.playback

import android.app.Activity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.gms.cast.framework.CastContext
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.di.AppScope
import javax.inject.Inject

@AppScope
class PlayerFactory @Inject constructor(private val localPlayer: SimpleExoPlayer) : CastPlayer.SessionAvailabilityListener {
    private val currentlyPlayingRelay: Relay<Player> = BehaviorRelay.create()
    private var castPlayer: CastPlayer? = null
        set(value) {
            if (value != field) {
                field?.setSessionAvailabilityListener(null)
                currentlyPlayingRelay.accept(if (value?.isCastSessionAvailable == true) castPlayer else localPlayer)
                value?.setSessionAvailabilityListener(this)
            }
            field = value
        }

    val playback: Observable<Player>
        get() = currentlyPlayingRelay.hide()

    override fun onCastSessionAvailable() {
        currentlyPlayingRelay.accept(castPlayer)
    }

    override fun onCastSessionUnavailable() {
        currentlyPlayingRelay.accept(localPlayer)
    }
    /** Fix for devices that don't have google play */
    fun bind(activity: Activity) {
        castPlayer = try {
            CastPlayer(CastContext.getSharedInstance(activity))
        } catch (e: Exception) {
            null
        }

    }

    fun release() {
        localPlayer.release()
        castPlayer?.release()
    }

}