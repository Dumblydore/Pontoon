package me.mauricee.pontoon.main

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
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
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
import me.mauricee.pontoon.ext.just
import me.mauricee.pontoon.model.livestream.LiveStreamInfo
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.Video
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
    var currentLiveStream: LiveStreamInfo? = null
        set(value) {
//            if (value?.id != field?.id && value != null) {
//                load("https://cdn1.floatplane.com${value.streamPath}/playlist.m3u8")
                MediaMetadataCompat.Builder()
//                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value.title)
//                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, value.description)
//                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value.creator.name)
//                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, value.video.thumbnail)
//                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, value.video.title)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value.video.duration)
//                        .build().apply(mediaSession::setMetadata)
//            } else if (value != null) {
//                player?.playWhenReady = true
//            } else {
//                player?.stop()
//                mediaSession.setMetadata(MediaMetadataCompat.Builder().build())
//            }
            field = value
        }
    var currentlyPlaying: Playback? = null
        set(value) {
            if (value?.video?.id != field?.video?.id && value != null) {
                load(value)
                MediaMetadataCompat.Builder()
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value.video.title)
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, value.video.description)
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value.video.creator.name)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, value.video.thumbnail)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, value.video.title)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value.video.duration)
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

    var quality: QualityLevel = preferences.defaultQualityLevel
        set(value) {
            if (value != field) {
                currentlyPlaying?.apply {
                    val progress = player?.currentPosition ?: 0
                    when (value) {
                        QualityLevel.p1080 -> this.quality.p1080
                        QualityLevel.p720 -> this.quality.p720
                        QualityLevel.p480 -> this.quality.p480
                        QualityLevel.p360 -> this.quality.p360
                    }.let(String::toUri).also { load(it, video) }
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
                load(this.quality.p1080.toUri(), video, oldPlayer?.currentPosition ?: 0, mediaSession.isActive)
            }
        }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        state = PlaybackStateCompat.STATE_ERROR
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

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    fun release() {
//        playerFactory.release()
//        mediaSession.release()
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
        previewImageRelay.accept(video.thumbnail)
        timelineSubject.accept("https://cms.linustechtips.com/get/sprite/by_guid/${video.id}")
    }

    private fun load(playback: Playback) {
        when (quality) {
            QualityLevel.p1080 -> playback.quality.p1080
            QualityLevel.p720 -> playback.quality.p720
            QualityLevel.p480 -> playback.quality.p480
            QualityLevel.p360 -> playback.quality.p360
        }.toUri().let { load(it, playback.video) }

        setMetadata(playback.video)
        previewImageRelay.accept(playback.video.thumbnail)
    }

    private fun load(uri: Uri, video: Video, startAt: Long = 0, autoPlay: Boolean = true) {
        (player as? ExoPlayer)?.just {
            prepare(networkSourceFactory.createMediaSource(uri))
        }
        (player as? CastPlayer)?.just {

            val metaData = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                putString(MediaMetadata.KEY_TITLE, video.title)
                putString(MediaMetadata.KEY_SUBTITLE, video.creator.name)
                addImage(WebImage(video.thumbnail.toUri()))
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


    enum class QualityLevel {
        p1080,
        p720,
        p480,
        p360,
    }

    enum class ViewMode {
        PictureInPicture,
        FullScreen,
        Expanded
    }

//    open class Playback() {
//
//    }
}