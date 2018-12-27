package me.mauricee.pontoon.player.player

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.video.VideoListener
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_player.view.*
import kotlinx.android.synthetic.main.layout_player_controls.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.NumberUtil
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.glide.GlideApp

class PlayerView : FrameLayout, VideoListener, Player.EventListener {

    private val behavioRelay: Relay<String> = BehaviorRelay.create()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.layout_player, this)
    }

    val ratio: Observable<String>
        get() = behavioRelay.hide()

    var player: SimpleExoPlayer? = null
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

    private fun unregisterPlayer(player: SimpleExoPlayer) {
        player.removeVideoListener(this)
        player.setVideoSurfaceView(null)
        player.removeListener(this)
    }

    private fun registerPlayer(player: SimpleExoPlayer) {
        player.addListener(this)
        player.addVideoListener(this)
        player.setVideoSurfaceView(player_content_surface)
        updateBuffering(false)
        updateErrorMsg(false)
        updateForCurrentTrackSelections(true)
    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        val realWidth = width * pixelWidthHeightRatio
        val videoAspectRatio = if (height == 0 || width == 0) 1f else realWidth / height
        val ratio = NumberUtil.asFraction(realWidth.toLong(), height.toLong(), ":")
        player_content.setAspectRatio(videoAspectRatio)
        behavioRelay.accept(ratio)
    }

    override fun onRenderedFirstFrame() {
        player_content_loader.isVisible = false
        player_content_preview.isVisible = false
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onSeekProcessed() {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        updateForCurrentTrackSelections()

    }

    override fun onPlayerError(error: ExoPlaybackException) {
        updateErrorMsg(true)
    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPositionDiscontinuity(reason: Int) {

    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

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

    fun showController(animate: Boolean = true) {
        if (animate) {
            player_controls.isVisible = true
            player_controls.animate()
                    .setDuration(250)
                    .alpha(1f)
                    .start()
        } else {
            player_controls.isVisible = true
        }
    }

    fun hideController(animate: Boolean = true) {
        if (animate) {
            player_controls.animate()
                    .setDuration(250)
                    .alpha(0f)
                    .withStartAction { player_controls.alpha = 1f }
                    .withEndAction { player_controls.isVisible = false }
                    .start()
        } else {
            player_controls.isVisible = false
        }
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
