package me.mauricee.pontoon.main

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.LiveStreamMetadata
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.player.player.PlayerView
import me.mauricee.pontoon.rx.context.BroadcastEvent
import me.mauricee.pontoon.rx.context.registerReceiver
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class Player @Inject constructor(preferences: Preferences,
                                 private val exoPlayer: SimpleExoPlayer,
                                 private val networkSourceFactory: HlsMediaSource.Factory,
                                 private val context: Context,
                                 private val mediaSession: MediaSessionCompat) : MediaSessionCompat.Callback(),
        Player.EventListener, LifecycleObserver {

    @PlaybackStateCompat.State
    private var state: Int = PlaybackStateCompat.STATE_NONE
        set(value) {
            if (field != value) {
                field = value
                stateSubject.accept(value)
                PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setState(value, exoPlayer.currentPosition, 1f)
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
    private var controllerTimeout: Disposable? = null
    private val controllerSubject = PublishRelay.create<ControlEvent>()
    val controllerEvent: Observable<ControlEvent> = controllerSubject.hide().startWith(ControlEvent.ControlsVisibilityChanged(false))
    var currentLiveStream: LiveStreamMetadata? = null
        set(value) {
            if (value?.id != field?.id && value != null) {
                load("https://cdn1.floatplane.com${value.streamPath}/playlist.m3u8".toUri())
                MediaMetadataCompat.Builder()
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value.title)
                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, value.description)
//                        .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value.creator.name)
//                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, value.video.thumbnail)
//                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, value.video.title)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, value.video.duration)
                        .build().apply(mediaSession::setMetadata)
            } else if (value != null) {
                exoPlayer.playWhenReady = true
            } else {
                exoPlayer.stop()
                mediaSession.setMetadata(MediaMetadataCompat.Builder().build())
            }
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
                exoPlayer.playWhenReady = true
            } else {
                exoPlayer.stop()
                mediaSession.setMetadata(MediaMetadataCompat.Builder().build())
            }
            field = value
        }

    var viewMode: ViewMode = ViewMode.Expanded
        set(value) {
            if (field != value) {
                field = value
                controlsVisible = value == ViewMode.Expanded
            }
        }

    var controlsVisible: Boolean = false
        set(value) {
            field = value
            notifyController(field, viewMode)
        }

    var quality: QualityLevel = preferences.defaultQualityLevel
        set(value) {
            if (value != field) {
                currentlyPlaying?.apply {
                    val progress = exoPlayer.currentPosition
                    when (value) {
                        QualityLevel.p1080 -> this.quality.p1080
                        QualityLevel.p720 -> this.quality.p720
                        QualityLevel.p480 -> this.quality.p480
                        QualityLevel.p360 -> this.quality.p360
                    }.let(String::toUri).also { load(it) }
                    exoPlayer.seekTo(progress)
                }
            }
            field = value
        }

    init {
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                .setActions(PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY)
                .build())
        bind()
//        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun isPlaying() = state == PlaybackStateCompat.STATE_PLAYING

    fun isActive(): Boolean = state != PlaybackStateCompat.STATE_NONE && state != PlaybackStateCompat.STATE_STOPPED

    fun bindToView(view: PlayerView) {
        view.player = exoPlayer
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
                durationRelay.accept(exoPlayer.duration)
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
        exoPlayer.playWhenReady = true
        state = PlaybackStateCompat.STATE_PLAYING
    }

    override fun onPause() {
        if (isActive()) {
            exoPlayer.playWhenReady = false
            state = PlaybackStateCompat.STATE_PAUSED
        }
    }

    override fun onStop() {
        currentlyPlaying = null
        state = PlaybackStateCompat.STATE_STOPPED
    }

    override fun onSeekTo(pos: Long) {
        exoPlayer.seekTo(pos)
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
        exoPlayer.release()
        mediaSession.release()
    }

    fun playPause() {
        if (exoPlayer.playWhenReady) onPause() else onPlay()
    }

    fun progress(): Observable<Long> = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .flatMap {
                Observable.fromCallable { exoPlayer.currentPosition }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
            }
            .startWith(exoPlayer.currentPosition)

    fun bufferedProgress(): Observable<Long> = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map { exoPlayer.bufferedPosition }.startWith(exoPlayer.bufferedPosition)

    fun toggleControls() {
        if (viewMode == ViewMode.PictureInPicture) return
        controllerTimeout?.dispose()
        controllerTimeout = (if (controlsVisible) false.toObservable()
        else Observable.timer(3, TimeUnit.SECONDS, Schedulers.computation()).map { false }
                .startWith(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { controlsVisible = it }.also { subs += it }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun bind() {
        mediaSession.setCallback(this)
        exoPlayer.addListener(this)
        subs += context.registerReceiver(IntentFilter(Intent.ACTION_HEADSET_PLUG))
                .map(BroadcastEvent::intent).subscribe(this::handleHeadsetChanges)
        mediaSession.isActive = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        subs.clear()
        mediaSession.setCallback(null)
        exoPlayer.removeListener(this)
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
        }.toUri().let(::load)

        setMetadata(playback.video)
        previewImageRelay.accept(playback.video.thumbnail)
    }

    private fun load(uri: Uri) {
        logd("loading $uri")
        exoPlayer.prepare(networkSourceFactory.createMediaSource(uri))
        exoPlayer.playWhenReady = true
    }

    private fun notifyController(isVisible: Boolean, viewMode: ViewMode) {
        controllerSubject.accept(ControlEvent.ControlsVisibilityChanged(isVisible))
        when (viewMode) {
            ViewMode.FullScreen -> {
                controllerSubject.accept(ControlEvent.ProgressVisibilityChanged(isVisible))
                controllerSubject.accept(ControlEvent.DisplayFullscreenIcon(true))
            }
            ViewMode.PictureInPicture -> {
                controllerSubject.accept(ControlEvent.AcceptUserInputChanged(isVisible))
                controllerSubject.accept(ControlEvent.DisplayFullscreenIcon(false))
            }
            ViewMode.Expanded -> {
                controllerSubject.accept(ControlEvent.ProgressVisibilityChanged(true))
                controllerSubject.accept(ControlEvent.DisplayFullscreenIcon(false))
            }
        }
    }

    enum class QualityLevel {
        p1080,
        p720,
        p480,
        p360
    }

    enum class ViewMode {
        PictureInPicture,
        FullScreen,
        Expanded
    }
}

sealed class ControlEvent {
    data class ControlsVisibilityChanged(val isVisible: Boolean) : ControlEvent()
    data class ProgressVisibilityChanged(val isVisible: Boolean) : ControlEvent()
    data class AcceptUserInputChanged(val canAccept: Boolean) : ControlEvent()
    data class DisplayFullscreenIcon(val isFullscreen: Boolean) : ControlEvent()
}