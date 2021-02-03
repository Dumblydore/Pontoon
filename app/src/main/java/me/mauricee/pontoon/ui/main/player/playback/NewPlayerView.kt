package me.mauricee.pontoon.ui.main.player.playback

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import com.google.android.exoplayer2.video.VideoListener
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStopChangeEvent
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_player.view.*
import kotlinx.android.synthetic.main.layout_player_controls.*
import kotlinx.android.synthetic.main.layout_player_controls.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.hide
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.ext.mainExecutor
import me.mauricee.pontoon.ext.show
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.playback.PontoonMetadata
import me.mauricee.pontoon.playback.WrappedExoPlayer
import me.mauricee.pontoon.rx.Optional
import me.mauricee.pontoon.rx.glide.toSingle
import java.util.concurrent.TimeUnit

class NewPlayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), Player.EventListener, VideoListener {

    private val sessionPlayerCallback = SessionPlayerCallback()
    private val viewSubscriptions = CompositeDisposable()
    private val controlsRunnable: Runnable = Runnable { controlsVisible = !controlsVisible }

    private val playIconAnimation by lazy { AppCompatResources.getDrawable(context, R.drawable.avc_play_to_pause) }
    private val playIcon by lazy { AppCompatResources.getDrawable(context, R.drawable.ic_play) }
    private val pauseIconAnimation by lazy { AppCompatResources.getDrawable(context, R.drawable.avc_pause_to_play) }
    private val pauseIcon by lazy { AppCompatResources.getDrawable(context, R.drawable.ic_pause) }

    @Volatile
    private var isSeeking: Boolean = false
    private var progressSubscription: Disposable? = null
        set(value) {
            field?.dispose()
            field = value?.also { viewSubscriptions += it }
        }
    private var exoPlayer: WrappedExoPlayer? = null
        set(value) {
            field?.also {
                it.removeListener(this)
                it.removeVideoListener(this)
            }
            field = value?.also {
                it.addListener(this)
                it.addVideoListener(this)
                it.setVideoTextureView(player_content_surface)
            }
        }
    private var sessionPlayer: SessionPlayerConnector? = null
        set(value) {
            field?.unregisterPlayerCallback(sessionPlayerCallback)
            field = value?.apply {
                registerPlayerCallback(context.mainExecutor(), sessionPlayerCallback)
            }
        }
    var controlsVisible: Boolean
        get() = player_controls.isVisible
        set(value) {
            player_controls.isVisible = value
        }

    init {
        View.inflate(context, R.layout.layout_player, this)
    }

    fun setSession(sessionPlayer: SessionPlayerConnector, exoPlayer: WrappedExoPlayer) {
        this.sessionPlayer = sessionPlayer
        this.exoPlayer = exoPlayer
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        viewSubscriptions += player_controls_playPause.clicks().subscribe {
            when (sessionPlayer?.playerState) {
                SessionPlayer.PLAYER_STATE_PAUSED -> sessionPlayer?.play()
                SessionPlayer.PLAYER_STATE_PLAYING -> sessionPlayer?.pause()
            }
        }
        viewSubscriptions += player_controls_progress.seekBarChanges.subscribe {
            player_controls_progress.thumbVisibility = when (it) {
                is SeekBarStartChangeEvent -> true
                is SeekBarStopChangeEvent -> {
                    sessionPlayer?.seekTo(it.view().progress.toLong())
                    false
                }
                else -> false
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        exoPlayer = null
        sessionPlayer = null
        viewSubscriptions.dispose()
    }

    override fun onRenderedFirstFrame() {
        super.onRenderedFirstFrame()
        exoPlayer?.currentTrackGroups?.let {
            for (i in 0 until it.length) {
                val group = it.get(i)
                for (y in 0 until group.length) {
                    val format = group.getFormat(y)
                    logd("ExoPlayer tracks: $format")
                }
            }
        }
//        ${exoPlayer?.currentTrackGroups ?: 0}
    }

    private fun startProgressWatcher(sessionPlayer: SessionPlayer) {
        progressSubscription = Observable.interval(0, 1000, TimeUnit.MILLISECONDS).map {
            sessionPlayer.currentPosition to sessionPlayer.bufferedPosition
        }.filter { !isSeeking }.subscribe {
            player_controls_progress.apply {
                progress = it.first
                bufferedProgress = it.second
            }
        }
    }

    fun showController() {
        handler?.removeCallbacks(controlsRunnable)
        player_controls.show { handler?.postDelayed(controlsRunnable, 5000) }
    }

    fun hideController() {
        handler?.removeCallbacks(controlsRunnable)
        player_controls.hide()
    }


    private inner class SessionPlayerCallback : SessionPlayer.PlayerCallback() {
        @SuppressLint("WrongConstant")
        override fun onCurrentMediaItemChanged(player: SessionPlayer, item: MediaItem) {
            item.metadata?.let { metadata ->
                player_controls_progress.duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
                metadata.getString(PontoonMetadata.TimeLineUri)?.let { timelineUri ->
                    viewSubscriptions += GlideApp.with(this@NewPlayerView).asBitmap().load(timelineUri).toSingle()
                            .map { Optional.of(it) }.onErrorReturnItem(Optional.empty())
                            .subscribe { it -> player_controls_progress.timelineBitmap = it.value }
                }
            }
        }

        override fun onPlayerStateChanged(player: SessionPlayer, playerState: Int) {
            when (playerState) {
                SessionPlayer.PLAYER_STATE_PLAYING -> {
                    startProgressWatcher(player)
                    setPlayingIcon(if (player_controls_playPause.isVisible) playIconAnimation else pauseIcon)
                }
                SessionPlayer.PLAYER_STATE_PAUSED -> {
                    setPlayingIcon(if (player_controls_playPause.isVisible) pauseIconAnimation else playIcon)
                }
                else -> progressSubscription = null
            }
        }

        override fun onBufferingStateChanged(player: SessionPlayer, item: MediaItem?, buffState: Int) {
            val isLoading = when (buffState) {
                SessionPlayer.BUFFERING_STATE_BUFFERING_AND_STARVED -> true
                SessionPlayer.BUFFERING_STATE_BUFFERING_AND_PLAYABLE -> false
                else -> false
            }
            player_content_loader.isVisible = isLoading
            if (isLoading) hideController() else showController()
        }

        private fun setPlayingIcon(icon: Drawable?) {
            player_controls_playPause.setImageDrawable(icon)
            (icon as? Animatable)?.start()
        }
    }

    fun fullscreenClicked(): Observable<Unit> = player_controls_fullscreen.clicks()
            .map { Unit }

//    private val controlsRunnable: Runnable = Runnable { controlsVisible = !controlsVisible }
//    private val borderRunnable: Runnable = Runnable { hideBorder() }
//    private val controlsVisibleRelay: Relay<Boolean> = PublishRelay.create()
//    private val ratioRelay: Relay<String> = BehaviorRelay.create()
//
//    private var maxScale = 0f
//    private var currentScale = 1f
//
//    val ratio: Observable<String>
//        get() = ratioRelay.hide()
//    val controlsVisibilityChanged: Observable<Boolean>
//        get() = controlsVisibleRelay.hide()
//
//    var player: Player? = null
//        set(value) {
//            if (field != value) {
//                field?.apply(::unregisterPlayer)
//            }
//            field = value
//            value?.let(::registerPlayer)
//        }
//
//    var isInFullscreen: Boolean = false
//        set(value) {
//            field = value
//            player_controls_fullscreen.setImageDrawable((if (field) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen).let { resources.getDrawable(it, null) })
//        }
//
//    var controlsVisible: Boolean = false
//        set(value) {
//            if (field != value) {
//                field = value
//                if (value) showController() else hideController()
//                controlsVisibleRelay.accept(field)
//            }
//        }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        getActivity()?.apply {
//            player_content.resizeMode = if (getDeviceWidth() > getDeviceHeight()) {
//                AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
//            } else {
//                AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
//            }
//        }
//        player_controls.isVisible = false
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        handler?.removeCallbacks(controlsRunnable)
//        handler?.removeCallbacks(borderRunnable)
//    }
//
//    fun scaleVideo(scaleTo: Float) {
//        currentScale = scaleTo
//        val newScale = Math.max(1f, Math.min(maxScale, currentScale))
//        player_content.scaleX = newScale
//        player_content.scaleY = newScale
//        if (newScale != maxScale && (newScale / maxScale) * 100f >= 90)
//            showBorder()
//    }
//
//    private fun unregisterPlayer(player: Player) {
//        (player as? SimpleExoPlayer)?.with {
//            it.removeVideoListener(this)
//            it.setVideoSurfaceView(null)
//        }
//        player.removeListener(this)
//    }
//
//    private fun registerPlayer(player: Player) {
//        player.addListener(this)
//        (player as? SimpleExoPlayer)?.with {
//            player.addVideoListener(this)
////            player.setVideoTextureView(
////                    player_content_surface)
//            onRenderedFirstFrame()
//
//        }
//        updateBuffering(false)
//        updateErrorMsg(false)
//        updateForCurrentTrackSelections(true)
//    }
//
//    private fun showBorder() {
//        handler?.removeCallbacks(borderRunnable)
//        player_border.show { handler?.postDelayed(borderRunnable, 1000) }
//    }
//
//    private fun hideBorder() {
//        player_border.hide()
//    }
//
//    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
//        val viewRatio = measuredWidth.toFloat() / measuredHeight.toFloat()
//        val realWidth = width * pixelWidthHeightRatio
//        val videoAspectRatio = if (height == 0 || width == 0) 1f else realWidth / height
//        val ratio = (realWidth.toLong() to height.toLong()).asFraction(":")
//        player_content.setAspectRatio(videoAspectRatio)
//        maxScale = Math.round((1 + (1 - (videoAspectRatio / viewRatio))) * 100f) / 100f
//        ratioRelay.accept(ratio)
//    }
//
//    override fun onRenderedFirstFrame() {
//        player_content_loader.isVisible = false
//        player_content_preview.isVisible = false
//    }
//
//    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
//        updateForCurrentTrackSelections()
//    }
//
//    override fun onPlayerError(error: ExoPlaybackException) {
//        updateErrorMsg(true)
//    }
//
//    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//        post {
//            when (playbackState) {
//                Player.STATE_BUFFERING -> updateBuffering(true)
//                Player.STATE_READY -> {
//                    updateBuffering(true)
//                }
//                Player.STATE_IDLE -> updateErrorMsg(true)
//            }
//        }
//    }
//
//
//    fun setThumbnail(previewArt: String) {
//        GlideApp.with(this).load(previewArt).placeholder(R.drawable.ic_default_thumbnail)
//                .error(R.drawable.ic_default_thumbnail)
//                .into(player_content_preview)
//    }
//
//    private fun showController() {
//        handler?.removeCallbacks(controlsRunnable)
//        player_controls.show { handler?.postDelayed(controlsRunnable, 5000) }
//    }
//
//    private fun hideController() {
//        handler?.removeCallbacks(controlsRunnable)
//        player_controls.hide()
//    }
//
//    private fun updateForCurrentTrackSelections(isNewPlayer: Boolean = false) {
//        if (isNewPlayer || player?.currentTrackGroups?.isEmpty == true) {
//            player_content_preview.isVisible = true
//        }
//    }
//
//    private fun updateBuffering(animate: Boolean) {
//        player?.let {
//            player_content_loader.isVisible = it.playbackState == Player.STATE_BUFFERING
//        }
//    }
//
//    private fun updateErrorMsg(animate: Boolean) {
//        player?.playbackError?.let {
//            player_content_preview.setImageDrawable(ColorDrawable(Color.BLACK))
//            player_content_loader.isVisible = true
//            player_controls_error.text = it.localizedMessage
//            player_controls_error.isVisible = true
//        }
//    }
}