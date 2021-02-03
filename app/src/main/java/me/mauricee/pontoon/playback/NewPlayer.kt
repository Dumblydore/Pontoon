package me.mauricee.pontoon.playback

import android.media.session.PlaybackState
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.session.MediaController
import androidx.media2.session.MediaSession

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import com.jakewharton.rx.replayingShare
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.rx.Optional
import me.mauricee.pontoon.rx.media.SessionPlayerEvent
import me.mauricee.pontoon.rx.media.playerCallbackEvents
import me.mauricee.pontoon.ui.main.player.playback.NewPlayerView
import java.lang.RuntimeException
import javax.inject.Inject

@ActivityRetainedScoped
class NewPlayer @Inject constructor(private val exoPlayer: WrappedExoPlayer,
                                    private val playerConnector: SessionPlayerConnector,
                                    private val session: MediaSession,
                                    private val controller: MediaController,
                                    private val castPlayer: Optional<CastPlayer>) : LifecycleObserver {

    private val callbackEvents: Observable<SessionPlayerEvent> by lazy {
        session.player.playerCallbackEvents()
                .doOnNext { logd("SessionPlayer event: ${it.javaClass.simpleName}") }
                .replayingShare()
    }

    private val activeMediaItem: Observable<MediaItem>
        get() = callbackEvents.filter {
            it is SessionPlayerEvent.CurrentMediaItemChangedEvent || it is SessionPlayerEvent.BufferingStateChangedEvent
        }.flatMapMaybe { event ->
            when (event) {
                is SessionPlayerEvent.BufferingStateChangedEvent -> event.item?.let { Maybe.just(it) }
                        ?: Maybe.empty()
                is SessionPlayerEvent.CurrentMediaItemChangedEvent -> Maybe.just(event.item)
                else -> throw RuntimeException("Shouldn't get this far")
            }
        }.distinctUntilChanged()

    val activeVideoId: Observable<String>
        get() = activeMediaItem.map { it.metadata?.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)!! }
    val supportedQuality: Observable<Set<Quality>>
        get() = activeMediaItem.map {
            it.metadata?.extras?.getParcelableArrayList<Quality>(PontoonMetadata.QualityLevelsKey)?.toSet()
                    ?: emptySet()
        }

    val isPlayingLocally: Boolean
        get() = exoPlayer.activePlayer is SimpleExoPlayer

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
    }

    fun playItem(videoId: String): Completable = Completable.fromAction {
        controller.setMediaItem(videoId).get()
        controller.seekTo(0)
        controller.play()
    }.subscribeOn(Schedulers.computation())

    fun togglePlayPause(): Completable = Completable.fromAction {
        if (exoPlayer.isPlaying)
            exoPlayer.pause()
        else
            exoPlayer.playWhenReady = true
    }

    fun pause(): Completable = Completable.fromAction { controller.pause() }

    fun stop(): Completable = Completable.fromAction { controller.pause() }

    fun bindToPlayer(view: NewPlayerView) {
        view.setSession(playerConnector, exoPlayer)
    }

    fun setQuality(quality: Quality) {
        exoPlayer.setMediaItem(com.google.android.exoplayer2.MediaItem.fromUri(quality.url), false)
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

    @Parcelize
    data class Quality(val label: String, val url: String) : Parcelable, Diffable<String> {
        override val id: String
            get() = label

    }


}