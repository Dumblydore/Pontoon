package me.mauricee.pontoon.ui.main.player.playback

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoListener
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_player.view.*
import kotlinx.android.synthetic.main.layout_player_controls.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.*
import me.mauricee.pontoon.glide.GlideApp

class PlayerView : FrameLayout, VideoListener, Player.EventListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.layout_player, this)
    }

    private val controlsRunnable: Runnable = Runnable { controlsVisible = !controlsVisible }
    private val borderRunnable: Runnable = Runnable { hideBorder() }
    private val controlsVisibleRelay: Relay<Boolean> = PublishRelay.create()
    private val ratioRelay: Relay<String> = BehaviorRelay.create()

    private var maxScale = 0f
    private var currentScale = 1f

    val ratio: Observable<String>
        get() = ratioRelay.hide()
    val controlsVisibilityChanged: Observable<Boolean>
        get() = controlsVisibleRelay.hide()

    var player: Player? = null
        set(value) {
            if (field != value) {
                field?.apply(::unregisterPlayer)
            }
            field = value
            value?.let(::registerPlayer)
        }

    var isInFullscreen: Boolean = false
        set(value) {
            field = value
            player_controls_fullscreen.setImageDrawable((if (field) R.drawable.ic_fullscreen_exit else R.drawable.ic_fullscreen).let { resources.getDrawable(it, null) })
        }

    var controlsVisible: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) showController() else hideController()
                controlsVisibleRelay.accept(field)
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        getActivity()?.apply {
            player_content.resizeMode = if (getDeviceWidth() > getDeviceHeight()) {
                AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            }
        }
        player_controls.isVisible = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler?.removeCallbacks(controlsRunnable)
        handler?.removeCallbacks(borderRunnable)
    }

    fun scaleVideo(scaleTo: Float) {
        currentScale = scaleTo
        val newScale = Math.max(1f, Math.min(maxScale, currentScale))
        player_content.scaleX = newScale
        player_content.scaleY = newScale
        if (newScale != maxScale && (newScale / maxScale) * 100f >= 90)
            showBorder()
    }

    private fun unregisterPlayer(player: Player) {
        (player as? SimpleExoPlayer)?.with {
            it.removeVideoListener(this)
            it.setVideoSurfaceView(null)
        }
        player.removeListener(this)
    }

    private fun registerPlayer(player: Player) {
        player.addListener(this)
        (player as? SimpleExoPlayer)?.with {
            player.addVideoListener(this)
//            player.setVideoTextureView(
//                    player_content_surface)
            onRenderedFirstFrame()

        }
        updateBuffering(false)
        updateErrorMsg(false)
        updateForCurrentTrackSelections(true)
    }

    private fun showBorder() {
        handler?.removeCallbacks(borderRunnable)
        player_border.show { handler?.postDelayed(borderRunnable, 1000) }
    }

    private fun hideBorder() {
        player_border.hide()
    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        val viewRatio = measuredWidth.toFloat() / measuredHeight.toFloat()
        val realWidth = width * pixelWidthHeightRatio
        val videoAspectRatio = if (height == 0 || width == 0) 1f else realWidth / height
        val ratio = (realWidth.toLong() to height.toLong()).asFraction(":")
        player_content.setAspectRatio(videoAspectRatio)
        maxScale = Math.round((1 + (1 - (videoAspectRatio / viewRatio))) * 100f) / 100f
        ratioRelay.accept(ratio)
    }

    override fun onRenderedFirstFrame() {
        player_content_loader.isVisible = false
        player_content_preview.isVisible = false
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        updateForCurrentTrackSelections()
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        updateErrorMsg(true)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        post {
            when (playbackState) {
                Player.STATE_BUFFERING -> updateBuffering(true)
                Player.STATE_READY -> {
                    updateBuffering(true)
                }
                Player.STATE_IDLE -> updateErrorMsg(true)
            }
        }
    }


    fun setThumbnail(previewArt: String) {
        GlideApp.with(this).load(previewArt).placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(player_content_preview)
    }

    private fun showController() {
        handler?.removeCallbacks(controlsRunnable)
        player_controls.show { handler?.postDelayed(controlsRunnable, 5000) }
    }

    private fun hideController() {
        handler?.removeCallbacks(controlsRunnable)
        player_controls.hide()
    }

    private fun updateForCurrentTrackSelections(isNewPlayer: Boolean = false) {
        if (isNewPlayer || player?.currentTrackGroups?.isEmpty == true) {
            player_content_preview.isVisible = true
        }
    }

    private fun updateBuffering(animate: Boolean) {
        player?.let {
            player_content_loader.isVisible = it.playbackState == Player.STATE_BUFFERING
        }
    }

    private fun updateErrorMsg(animate: Boolean) {
        player?.playbackError?.let {
            player_content_preview.setImageDrawable(ColorDrawable(Color.BLACK))
            player_content_loader.isVisible = true
            player_controls_error.text = it.localizedMessage
            player_controls_error.isVisible = true
        }
    }
}
