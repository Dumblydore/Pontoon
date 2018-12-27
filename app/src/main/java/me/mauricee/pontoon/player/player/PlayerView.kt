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
import kotlinx.android.synthetic.main.layout_player.view.*
import kotlinx.android.synthetic.main.layout_player_controls.view.*
import kotlinx.android.synthetic.main.view_timebar.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout

class PlayerView : FrameLayout, VideoListener, Player.EventListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.layout_player, this)
    }

    var player: SimpleExoPlayer? = null
        set(value) {
            if (field != value) {
                field?.apply(::unregisterPlayer)
            }
            field = value
            value?.let(::registerPlayer)
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
    }

    override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        val videoAspectRatio = if (height == 0 || width == 0) 1f else width * pixelWidthHeightRatio / height
        player_content.setAspectRatio(videoAspectRatio)
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

    fun showController() {
        player_controls.isVisible = true
    }

    fun hideController() {
        player_controls.isVisible = false
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
