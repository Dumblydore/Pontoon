package me.mauricee.pontoon.main

import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.TextureView
import androidx.core.net.toUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.video.Playback
import me.mauricee.pontoon.model.video.Video
import java.util.concurrent.TimeUnit

class Player(private val exoPlayer: SimpleExoPlayer,
             private val networkSourceFactory: HlsMediaSource.Factory,
             private val audioManager: AudioManager,
             private val sharedPreferences: SharedPreferences,
             private val mediaSession: MediaSessionCompat) : MediaSessionCompat.Callback(),
        Player.EventListener {

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

    private val stateSubject = BehaviorRelay.create<Int>()
    val playbackState: Observable<Int> = stateSubject

    private val previewImageRelay = BehaviorRelay.create<String>()
    val previewImage: Observable<String>
        get() = previewImageRelay

    private val durationRelay = BehaviorRelay.create<Long>()
    val duration: Observable<Long>
        get() = durationRelay

    var currentlyPlaying: Playback? = null
        set(value) {
            if (value?.video?.id != field?.video?.id && value != null) {
                load(value)
            } else {
                exoPlayer.stop()
            }
            field = value
        }

    private var controllerTimeout: Disposable? = null

    var controlsVisible: Boolean = false
        set(value) {
            field = value
            controller?.controlsVisible(value)
        }

    var controller: ControlView? = null
        set(value) {
            field = value
            field?.apply { controlsVisible(controlsVisible) }
        }

    var quality: QualityLevel = sharedPreferences.getString("settings_quality", "p1080")
            .let(QualityLevel::valueOf)
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
        mediaSession.setCallback(this)
        exoPlayer.addListener(this)
    }

    fun isPlaying() = state == PlaybackStateCompat.STATE_PLAYING

    fun isActive() = state != PlaybackStateCompat.STATE_NONE

    fun bindToView(view: TextureView) {
        exoPlayer.setVideoTextureView(view)
    }

    private fun setMetadata(video: Video) {
        previewImageRelay.accept(video.thumbnail)
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
        exoPlayer.prepare(networkSourceFactory.createMediaSource(uri))
        mediaSession.isActive = true
        exoPlayer.playWhenReady = true
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
//        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                .setAcceptsDelayedFocusGain(false)
//                .setWillPauseWhenDucked(true)
//                .build()
//        audioManager.requestAudioFocus()
        exoPlayer.playWhenReady = true
        state = PlaybackStateCompat.STATE_PLAYING
    }

    override fun onPause() {
        exoPlayer.playWhenReady = false
        state = PlaybackStateCompat.STATE_PAUSED
    }

    override fun onStop() {
        exoPlayer.playWhenReady = false
        exoPlayer.release()
        state = PlaybackStateCompat.STATE_STOPPED
    }

    override fun onSeekProcessed() {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    fun playPause() {
        if (exoPlayer.playWhenReady) onPause() else onPlay()
    }

    fun progress(): Observable<Long> = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map { exoPlayer.currentPosition }.startWith(exoPlayer.currentPosition)

    fun bufferedProgress(): Observable<Long> = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map { exoPlayer.bufferedPosition }.startWith(exoPlayer.bufferedPosition)

    fun toggleControls() {
        controllerTimeout?.dispose()
        controllerTimeout = (if (controlsVisible) false.toObservable()
        else Observable.timer(3, TimeUnit.SECONDS, Schedulers.computation()).map { false }
                .startWith(true))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { controller?.controlsVisible(it) }
    }

    interface ControlView {
        fun controlsVisible(isVisible: Boolean)
    }

    enum class QualityLevel {
        p1080,
        p720,
        p480,
        p360
    }
}