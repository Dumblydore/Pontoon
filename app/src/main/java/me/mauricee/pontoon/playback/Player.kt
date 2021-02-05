package me.mauricee.pontoon.playback

import android.os.Parcelable
import android.view.TextureView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
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
import kotlinx.android.parcel.Parcelize
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.rx.Optional
import me.mauricee.pontoon.rx.media.SessionPlayerEvent
import me.mauricee.pontoon.rx.media.playerCallbackEvents
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityRetainedScoped
class Player @Inject constructor(private val exoPlayer: WrappedExoPlayer,
                                 private val playerConnector: SessionPlayerConnector,
                                 private val session: MediaSession,
                                 private val controller: MediaController,
                                 private val castPlayer: Optional<CastPlayer>,
                                 prefs: Preferences) : LifecycleObserver {

    private var qualityLevel: String = prefs.defaultQualityLevel
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
    val selectedQualityLevel: Observable<Quality>
        get() = activeMediaItem.map { it.metadata?.extras?.getParcelable<Quality>(PontoonMetadata.CurrentQualityLevelKey)!! }
                .doOnNext { logd("activeMediaItem: $it") }

    val timelinePreviewUrl: Observable<String>
        get() = activeMediaItem.map { it.metadata?.extras?.getString(PontoonMetadata.TimeLineUri)!! }
                .doOnNext { logd("timelinePreviewUrl: $it") }

    val isLocalPlayer: Boolean
        get() = exoPlayer.activePlayer is SimpleExoPlayer

    val isReadyForPiP
        get() = isLocalPlayer && exoPlayer.isPlaying

    val isPlaying: Observable<Boolean>
        get() = callbackEvents.filter { it is SessionPlayerEvent.PlayerStateChangedEvent }
                .cast(SessionPlayerEvent.PlayerStateChangedEvent::class.java)
                .map { it.playerState == SessionPlayer.PLAYER_STATE_PLAYING }
    val duration: Observable<Long>
        get() = activeMediaItem.map { it.endPosition }
    val currentPosition: Observable<Long>
        get() = isPlaying.switchMap { isPlaying ->
            val pos = playerConnector.currentPosition
            if (!isPlaying) Observable.just(pos)
            else Observable.interval(1, TimeUnit.SECONDS)
                    .map { count -> pos + (1000 * count) }
                    .startWith(pos)
        }

    fun playItem(videoId: String): Completable = Completable.fromAction {
        controller.setMediaItem("$videoId:$qualityLevel")
        controller.seekTo(0)
        controller.play()
    }

    fun togglePlayPause(): Completable = Completable.fromAction {
        if (exoPlayer.isPlaying)
            exoPlayer.pause()
        else
            exoPlayer.playWhenReady = true
    }

    fun pause(): Completable = Completable.fromAction { controller.pause() }

    fun stop(): Completable = Completable.fromAction {
        controller.pause()
    }

    fun bindToTexture(view: TextureView) {
        exoPlayer.setVideoTextureView(view)
    }

    fun setQuality(quality: Quality): Completable = Completable.fromAction {
        val mediaId = controller.currentMediaItem?.metadata?.mediaId?.split(":")?.firstOrNull()
                ?: ""
        controller.setMediaItem("$mediaId:${quality.label}")
        qualityLevel = quality.label
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

    fun seekTo(position: Long): Completable = Completable.fromAction { controller.seekTo(position) }

    @Parcelize
    data class Quality(val label: String, val url: String) : Parcelable, Diffable<String> {
        override val id: String
            get() = label

        override fun toString(): String = label
    }
}