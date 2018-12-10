package me.mauricee.pontoon.common.playback

import android.content.Context
import android.view.TextureView
import androidx.core.net.toUri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable


//TODO maybe put media session in this?
class LocalPlayback(private val networkSourceFactory: HlsMediaSource.Factory, context: Context) : Playback,
        Player.EventListener {

    private val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
    private val eventRelay: Relay<Int> = PublishRelay.create()

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

    override fun prepare(mediaItem: Playback.MediaItem) {
        player.prepare(networkSourceFactory.createMediaSource(mediaItem.source.toUri()))
    }

    fun bindToView(view: TextureView) {
        player.setVideoTextureView(view)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSeekProcessed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPositionDiscontinuity(reason: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}