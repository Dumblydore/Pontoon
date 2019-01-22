package me.mauricee.pontoon.main

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.TextureView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.common.playback.LocalPlayback
import me.mauricee.pontoon.common.playback.Playback
import me.mauricee.pontoon.common.playback.PlaybackLocation
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.ext.just
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.model.audio.AudioFocusManager
import me.mauricee.pontoon.model.audio.FocusState
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.PlaybackMetadata
import me.mauricee.pontoon.model.video.Video
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AppScope
class Player @Inject constructor(private val preferences: Preferences,
                                 private val playbackFactory: Playback.Factory,
                                 private val focusManager: AudioFocusManager, private val context: Context,
                                 private val mediaSession: MediaSessionCompat) : MediaSessionCompat.Callback(), LifecycleObserver {

    @PlaybackStateCompat.State
    private var state: Int = PlaybackStateCompat.STATE_NONE
        set(value) {
            if (field != value) {
                field = value
                stateSubject.accept(value)
                PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setState(value, currentPlayback.position, 1f)
                        .build().also(mediaSession::setPlaybackState)
            }
        }

    private var currentPlayback: Playback = playbackFactory.initialPlayback

    private val subs = CompositeDisposable()

    private val locationRelay: Relay<PlaybackLocation> = BehaviorRelay.createDefault(currentPlayback.location)

    val playbackLocation: Observable<PlaybackLocation>
        get() = locationRelay.hide()

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

    //TODO it may be better to make this an observable
    private var isForeground = false

    val canGoIntoPip: Boolean
        get() = preferences.pictureInPicture.let { pip ->
            when {
                currentPlayback.location == PlaybackLocation.Remote -> false
                pip == Preferences.PictureInPicture.Always && isActive() -> true
                pip == Preferences.PictureInPicture.OnlyWhenPlaying && isPlaying() -> true
                else -> false
            }
        }

    var currentlyPlaying: PlaybackMetadata? = null
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
                currentPlayback.play()
            } else {
                currentPlayback.stop()
                mediaSession.setMetadata(MediaMetadataCompat.Builder().build())
            }
            field = value
        }

    private var controllerTimeout: Disposable? = null

    var viewMode: ViewMode = ViewMode.Expanded
        set(value) {
            if (field != value) {
                field = value
                controlsVisible = false
            }
        }

    var controlsVisible: Boolean = false
        set(value) {
            field = value
            controller?.apply {
                onControlsVisibilityChanged(field)
                notifyController(field, viewMode)
            }
        }

    var controller: ControlView? = null
        set(value) {
            field = value
            controlsVisible = false
        }

    var quality: QualityLevel = preferences.defaultQualityLevel
        set(value) {
            if (value != field) {
                currentlyPlaying?.apply {
                    val progress = currentPlayback.position
                    val source = when (value) {
                        QualityLevel.p1080 -> this.quality.p1080
                        QualityLevel.p720 -> this.quality.p720
                        QualityLevel.p480 -> this.quality.p480
                        QualityLevel.p360 -> this.quality.p360
                    }
                    currentPlayback.prepare(Playback.MediaItem(source, video, progress), true)
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
    }

    fun isPlaying() = state == PlaybackStateCompat.STATE_PLAYING

    fun isActive(): Boolean = state != PlaybackStateCompat.STATE_NONE && state != PlaybackStateCompat.STATE_STOPPED

    fun bindToView(view: TextureView) {
        (currentPlayback as? LocalPlayback)?.bindToView(view)
    }

    override fun onPlay() {
        currentPlayback.play()
        state = PlaybackStateCompat.STATE_PLAYING
        focusManager.gain()
    }

    override fun onPause() {
        if (isActive()) {
            currentPlayback.pause()
            state = PlaybackStateCompat.STATE_PAUSED
            focusManager.drop()
        }
    }

    override fun onStop() {
        currentlyPlaying = null
        state = PlaybackStateCompat.STATE_STOPPED
        focusManager.drop()
    }

    override fun onSeekTo(pos: Long) {
        currentPlayback.position = pos
        onPlay()
    }

    fun release() {
        currentPlayback.stop()
        focusManager.drop()
        mediaSession.release()
    }

    fun playPause() {
        if (state == PlaybackStateCompat.STATE_PLAYING)
            onPause()
        else if (state == PlaybackStateCompat.STATE_PAUSED)
            onPlay()
    }

    fun progress(): Observable<Long> = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .flatMapSingle { Single.fromCallable { currentPlayback.position }.subscribeOn(AndroidSchedulers.mainThread()) }
            .startWith(currentPlayback.position)

    fun bufferedProgress(): Observable<Long> = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .flatMapSingle { Single.fromCallable { currentPlayback.bufferedPosition }.subscribeOn(AndroidSchedulers.mainThread()) }
            .startWith(currentPlayback.bufferedPosition)

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
        subs += playbackFactory.playback
                .doOnNext { locationRelay.accept(it.location) }
                .map(this::switchPlayback)
                .startWith(currentPlayback)
                .flatMap(Playback::playerState)
                .subscribe { state = it }

        subs += focusManager.focus.subscribe { it ->
            when (it) {
                FocusState.Gained -> if (state != PlaybackStateCompat.STATE_PAUSED) currentPlayback.play()
                FocusState.Duck,
                FocusState.Transient,
                FocusState.Loss -> currentPlayback.pause()
            }
        }
        subs += context.registerReceiver(IntentFilter(Intent.ACTION_HEADSET_PLUG))
                .map(BroadcastEvent::intent).subscribe(this::handleHeadsetChanges)
        mediaSession.isActive = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clear() {
        subs.clear()
        mediaSession.setCallback(null)
        mediaSession.release()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeGround() {
        isForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onLifecyclePause() {
        if (currentPlayback.location == PlaybackLocation.Local) {
            onPause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        isForeground = false
    }


    private fun handleHeadsetChanges(intent: Intent) {
        if ((intent.getIntExtra("state", -1) == 0)) {
            onPause()
        }
    }

    private fun switchPlayback(newPlayback: Playback): Playback {
        currentlyPlaying?.with { newPlayback.prepare(Playback.MediaItem(it.quality.p1080, it.video, currentPlayback.position), isForeground) }
        currentPlayback.stop()
        currentPlayback = newPlayback
        return currentPlayback
    }

    //TODO Not sure if this is the best way of doing it. It might be better to have it as a part of Video.
    private fun setMetadata(video: Video) {
        previewImageRelay.accept(video.thumbnail)
        timelineSubject.accept("https://cms.linustechtips.com/get/sprite/by_guid/${video.id}")
    }

    private fun load(playback: PlaybackMetadata) {
        val source = when (quality) {
            QualityLevel.p1080 -> playback.quality.p1080
            QualityLevel.p720 -> playback.quality.p720
            QualityLevel.p480 -> playback.quality.p480
            QualityLevel.p360 -> playback.quality.p360
        }

        setMetadata(playback.video)
        currentPlayback.prepare(Playback.MediaItem(source, playback.video), true)
        focusManager.gain()
        previewImageRelay.accept(playback.video.thumbnail)
    }

    private fun notifyController(isVisible: Boolean, viewMode: ViewMode) = controller?.just {
        when (viewMode) {
            ViewMode.FullScreen -> {
                onProgressVisibilityChanged(isVisible)
                displayFullscreenIcon(true)
            }
            ViewMode.PictureInPicture -> {
                onAcceptUserInputChanged(isVisible)
                displayFullscreenIcon(false)
            }
            else -> displayFullscreenIcon(false)
        }
    }

    interface ControlView {
        fun onControlsVisibilityChanged(isVisible: Boolean)
        fun onProgressVisibilityChanged(isVisible: Boolean)
        fun onAcceptUserInputChanged(canAccept: Boolean)
        fun displayFullscreenIcon(isFullscreen: Boolean)
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