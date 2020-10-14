package me.mauricee.pontoon.playback

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleObserver
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.common.playback.PlayerFactory
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.VideoContentJson
import me.mauricee.pontoon.ext.just
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.Stream
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.get
import me.mauricee.pontoon.rx.context.BroadcastEvent
import me.mauricee.pontoon.rx.context.registerReceiver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class Player @Inject constructor(preferences: Preferences,
                                 private val networkSourceFactory: HlsMediaSource.Factory,
                                 private val context: Context,
                                 private val playerFactory: PlayerFactory,
                                 private val mediaSession: MediaSessionCompat) : MediaSessionCompat.Callback(),
        Player.EventListener, LifecycleObserver {
    @PlaybackStateCompat.State
    private var state: Int = PlaybackStateCompat.STATE_NONE
        set(value) {
            if (field != value) {
                field = value
                stateSubject.accept(value)
                PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setState(value, player?.currentPosition ?: 0, 1f)
                        .build().also(mediaSession::setPlaybackState)
            }
        }

    private val subs = CompositeDisposable()

    private val stateSubject = BehaviorRelay.create<Int>()
    val playbackState: Observable<Int> = stateSubject

    private val previewImageRelay = BehaviorRelay.create<String>()
    val previewImage: Observable<String>
        get() = previewImageRelay

    private val durationRelay = BehaviorRelay.create<Long>()
    val duration: Observable<Long>
        get() = durationRelay

    private val timelineSubject = BehaviorRelay.create<String>()
    val thumbnailTimeline: Observable<String>
        get() = timelineSubject
    private val viewModeRelay: Relay<ViewMode> = BehaviorRelay.create<ViewMode>()

    private var player: Player? = null
    var currentlyPlaying: Playback? = null
        set(value) {
            if (value?.video?.id != field?.video?.id && value != null) {
                load(value)
                MediaMetadataCompat.Builder()
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value.video.entity.title)
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, value.video.entity.description)
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value.video.creator.entity.name)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, value.video.entity.thumbnail)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, value.video.entity.title)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value.video.entity.duration)
                        .build().apply(mediaSession::setMetadata)
            } else if (value != null) {
                player?.playWhenReady = true
            } else {
                player?.stop()
                mediaSession.setMetadata(MediaMetadataCompat.Builder().build())
            }
            field = value
        }

    var viewMode: ViewMode = ViewMode.Expanded
        set(value) {
            if (field != value) {
                field = value
                viewModeRelay.accept(field)
            }
        }
    val viewModes: Observable<ViewMode>
        get() = viewModeRelay.hide()

    var quality: String = preferences.defaultQualityLevel
        set(value) {
            if (value != field) {
                val progress = player?.currentPosition ?: 0
                currentlyPlaying?.apply {
                    currentlyPlaying?.streams?.firstOrNull { it.name == field }?.url?.toUri()
                            ?.also { load(it, video) }
                    player?.seekTo(progress)
                }
            }
            field = value
        }

    val isPlaying: Boolean
        get() = state == PlaybackStateCompat.STATE_PLAYING

    val isActive: Boolean
        get() = state != PlaybackStateCompat.STATE_NONE && state != PlaybackStateCompat.STATE_STOPPED

    val isPlayingLocally: Boolean
        get() = player is SimpleExoPlayer

    init {
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                .setActions(PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY)
                .build())
        bind()
        subs += playerFactory.playback.subscribe {
            player?.playWhenReady = false
            player?.removeListener(this)
            it.addListener(this)
            val oldPlayer = player
            player = it
            currentlyPlaying?.apply {
                load((streams[quality]
                        ?: streams.first()).url.toUri(), video, oldPlayer?.currentPosition
                        ?: 0, mediaSession.isActive)
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        state = when (playbackState) {
            Player.STATE_READY -> {
                durationRelay.accept(player?.duration ?: 0)
                if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            }
            Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackStateCompat.STATE_NONE
            else -> PlaybackStateCompat.STATE_NONE
        }
    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onPlay() {
        player?.playWhenReady = true
        state = PlaybackStateCompat.STATE_PLAYING
    }

    override fun onPause() {
        if (isActive) {
            player?.playWhenReady = false
            state = PlaybackStateCompat.STATE_PAUSED
        }
    }

    override fun onStop() {
        currentlyPlaying = null
        state = PlaybackStateCompat.STATE_STOPPED
    }

    override fun onSeekTo(pos: Long) {
        player?.seekTo(pos)
        onPlay()
    }

    override fun onSeekProcessed() {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        state = PlaybackStateCompat.STATE_ERROR
    }

    fun release() {
        playerFactory.release()
        mediaSession.release()
    }

    fun playPause() {
        if (player?.playWhenReady == true) onPause() else onPlay()
    }

    fun progress(): Observable<Long> = playerFactory.playback.switchMap { currentPlayer ->
        Observable.interval(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.from(currentPlayer.applicationLooper))
                .map { currentPlayer.currentPosition }
                .subscribeOn(AndroidSchedulers.from(currentPlayer.applicationLooper))
                .startWith(currentPlayer.currentPosition)
    }

    fun bufferedProgress(): Observable<Long> = playerFactory.playback.switchMap { currentPlayer ->
        Observable.interval(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.from(currentPlayer.applicationLooper))
                .map { currentPlayer.bufferedPosition }
                .subscribeOn(AndroidSchedulers.from(currentPlayer.applicationLooper))
                .startWith(currentPlayer.bufferedPosition)
    }

    fun setQualityIndex(qualityIndex: Int): Stream? {
        return currentlyPlaying?.streams?.getOrNull(qualityIndex)?.also { quality = it.name }
    }

    fun bind() {
        mediaSession.setCallback(this)
        subs += context.registerReceiver(IntentFilter(Intent.ACTION_HEADSET_PLUG))
                .map(BroadcastEvent::intent).subscribe(this::handleHeadsetChanges)
    }

    fun onActive() {
        mediaSession.isActive = true
    }

    fun onInactive() {
        mediaSession.isActive = false
    }

    fun clear() {
        subs.clear()
        mediaSession.setCallback(null)
        player?.removeListener(this)
        mediaSession.release()
    }

    private fun handleHeadsetChanges(intent: Intent) {
        if ((intent.getIntExtra("state", -1) == 0)) {
            onPause()
        }
    }

    //TODO Not sure if this is the best way of doing it. It might be better to have it as a part of Video.
    private fun setMetadata(video: Video) {
        previewImageRelay.accept(video.entity.thumbnail)
        timelineSubject.accept("https://cms.linustechtips.com/get/sprite/by_guid/${video.id}")
    }

    //TODO reimplement quality lvls
    private fun load(playback: Playback) {
        load(playback.streams.first().url.toUri(), playback.video)
        setMetadata(playback.video)
        previewImageRelay.accept(playback.video.entity.thumbnail)
    }

    private fun load(uri: Uri, video: Video, startAt: Long = 0, autoPlay: Boolean = true) {
        (player as? ExoPlayer)?.just {
            setMediaSource(networkSourceFactory.createMediaSource(MediaItem.fromUri(uri)))
        }
        (player as? CastPlayer)?.just {

            val metaData = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                putString(MediaMetadata.KEY_TITLE, video.entity.title)
                putString(MediaMetadata.KEY_SUBTITLE, video.creator.entity.name)
                addImage(WebImage(video.entity.thumbnail.toUri()))
            }
            val info = MediaInfo.Builder(uri.toString())
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("application/x-mpegurl")
                    .setMetadata(metaData)
                    .build()
            loadItem(MediaQueueItem.Builder(info).setAutoplay(true).build(), startAt)
        }

        player?.playWhenReady = autoPlay
    }

    enum class ViewMode {
        PictureInPicture,
        FullScreen,
        Expanded
    }
}