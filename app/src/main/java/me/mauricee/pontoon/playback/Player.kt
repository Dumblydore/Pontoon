package me.mauricee.pontoon.playback

import android.content.Context
import android.os.Parcelable
import android.util.Rational
import android.view.TextureView
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaController
import androidx.media2.session.MediaSession
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.jakewharton.rx.replayingShare
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.parcelize.Parcelize
import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.log.logd
import me.mauricee.pontoon.preferences.Preferences
import me.mauricee.pontoon.rx.Optional
import me.mauricee.pontoon.rx.exoplayer.VideoEvent
import me.mauricee.pontoon.rx.exoplayer.observe
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
                                 private val descriptionAdapter: PlayerDescriptionAdapter,
                                 private val context: Context,
                                 private val prefs: Preferences) {

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
    val previewUrl: Observable<String>
        get() = activeMediaItem.map { it.metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI) }

    val isLocalPlayer: Boolean
        get() = exoPlayer.activePlayer is SimpleExoPlayer

    val isShowingNotification: Boolean
        get() = notificationManager != null

    val isReadyForPiP: Boolean
        get() = when (prefs.pictureInPicture) {
            Preferences.PictureInPicture.Always -> isLocalPlayer && controller.currentMediaItem != null
            Preferences.PictureInPicture.OnlyWhenPlaying -> isLocalPlayer && exoPlayer.isPlaying
            Preferences.PictureInPicture.Never -> false
        }

    val isPlaying: Observable<Boolean>
        get() = callbackEvents.filter { it is SessionPlayerEvent.PlayerStateChangedEvent }
                .cast(SessionPlayerEvent.PlayerStateChangedEvent::class.java)
                .map { it.playerState == SessionPlayer.PLAYER_STATE_PLAYING }
    val duration: Observable<Long>
        get() = activeMediaItem.map { it.endPosition }
    val currentPosition: Observable<Long>
        get() = Observable.interval(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.from(exoPlayer.applicationLooper))
                .map { exoPlayer.currentPosition }
                .subscribeOn(AndroidSchedulers.from(exoPlayer.applicationLooper))
                .startWith(exoPlayer.currentPosition)
                .distinctUntilChanged()

    val isBuffering: Observable<Boolean>
        get() = callbackEvents.filter { it is SessionPlayerEvent.BufferingStateChangedEvent }
                .cast(SessionPlayerEvent.BufferingStateChangedEvent::class.java)
                .map { it.buffState == SessionPlayer.BUFFERING_STATE_BUFFERING_AND_STARVED }

    val contentRatio: Observable<Rational>
        get() = exoPlayer.activePlayerChanged
                .map { exoPlayer.activePlayer }
                .startWith(exoPlayer.activePlayer)
                .filter { isLocalPlayer }
                .cast(SimpleExoPlayer::class.java)
                .switchMap { it.observe() }
                .filter { it is VideoEvent.OnVideoSizeChanged }
                .cast(VideoEvent.OnVideoSizeChanged::class.java)
                .map { Rational(it.width, it.height) }

    private var notificationManager: PlayerNotificationManager? = null

    fun playItem(videoId: String): Completable = Completable.fromAction {
        controller.seekTo(0)
        controller.setMediaItem("$videoId:$qualityLevel")
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

    fun startNotification() {
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context, "Pontoon", R.string.app_name, R.string.app_name, 101, descriptionAdapter).apply {
            setPlayer(exoPlayer)
            setUseNavigationActions(false)
            setMediaSessionToken(session.sessionCompatToken)
        }
    }

    fun stopNotification() {
        notificationManager?.setPlayer(null)
        notificationManager = null
    }

    fun release() {
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