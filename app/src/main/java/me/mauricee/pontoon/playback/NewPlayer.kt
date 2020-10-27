package me.mauricee.pontoon.playback

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.media2.session.MediaController
import androidx.media2.session.MediaSession
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.rx.Optional
import me.mauricee.pontoon.ui.main.MainScope
import me.mauricee.pontoon.ui.main.player.playback.NewPlayerView
import javax.inject.Inject

@MainScope
class NewPlayer @Inject constructor(lifecycle: LifecycleOwner,
                                    private val exoPlayer: WrappedExoPlayer,
                                    private val playerConnector: SessionPlayerConnector,
                                    private val session: MediaSession,
                                    private val controller: MediaController,
                                    private val castPlayer: Optional<CastPlayer>) : LifecycleObserver, MediaController.ControllerCallback() {

    init {
        lifecycle.lifecycle.addObserver(this)
    }

    val isPlayingLocally: Boolean
        get() = exoPlayer.activePlayer is SimpleExoPlayer

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
    }

    fun playItem(videoId: String): Completable = Completable.fromAction {
        controller.setMediaItem(videoId).get()
        controller.play()
    }.subscribeOn(Schedulers.computation())

    fun pause(): Completable = Completable.fromAction {
        controller.pause()
    }

    fun stop(): Completable = Completable.fromAction {
        controller.pause()
    }

    fun bindToPlayer(view: NewPlayerView) {
        view.setSession(playerConnector, exoPlayer)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        playerConnector.close()
        controller.close()
        session.close()
        castPlayer.value?.apply {
            setSessionAvailabilityListener(null)
            release()
        }
    }
}