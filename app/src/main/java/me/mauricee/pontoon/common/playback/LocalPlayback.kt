package me.mauricee.pontoon.common.playback

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.view.TextureView
import androidx.core.net.toUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.ext.prepareAnd


class LocalPlayback(private val networkSourceFactory: HlsMediaSource.Factory, context: Context) : Playback,
        Player.EventListener {

    private val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())

    private val eventRelay: Relay<Int> = BehaviorRelay.create()

    override val location: PlaybackLocation
        get() = PlaybackLocation.Local
    override val bufferedPosition: Long
        get() = player.bufferedPosition
    override val playerState: Observable<Int>
        get() = eventRelay.hide()
    override var position: Long
        get() = player.currentPosition
        set(value) {
            player.seekTo(value)
        }

    init {
        player.addListener(this)
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun play() {
        player.playWhenReady = true
    }

    override fun stop() {
        player.playWhenReady = false
        player.release()
    }

    override fun prepare(mediaItem: Playback.MediaItem, playOnPrepare: Boolean) {
        player.prepareAnd(networkSourceFactory.createMediaSource(mediaItem.source.toUri())) {
            it.seekTo(mediaItem.position)
        }
        player.playWhenReady = playOnPrepare
    }

    fun bindToView(view: TextureView) {
        player.setVideoTextureView(view)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onSeekProcessed() {
        eventRelay.accept(PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {

    }

    override fun onPlayerError(error: ExoPlaybackException) {
        eventRelay.accept(PlaybackStateCompat.STATE_ERROR)

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
        val state = when (playbackState) {
            Player.STATE_READY -> {
//                durationRelay.accept(player.duration)
                if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            }
            Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackStateCompat.STATE_NONE
            else -> PlaybackStateCompat.STATE_NONE
        }
        eventRelay.accept(state)
    }
}