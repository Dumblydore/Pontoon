package me.mauricee.pontoon.common.playback

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.gms.cast.framework.CastContext
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.ui.main.MainActivity
import me.mauricee.pontoon.ui.main.MainScope
import javax.inject.Inject

@MainScope
class PlayerFactory @Inject constructor(private val mainActivity: MainActivity, private val localPlayer: SimpleExoPlayer) : SessionAvailabilityListener, LifecycleObserver {

    init {
        mainActivity.lifecycle.addObserver(this)
    }

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

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        castPlayer = try {
            CastPlayer(CastContext.getSharedInstance(mainActivity))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bind(activity: Activity) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        localPlayer.release()
        castPlayer?.release()
    }

}