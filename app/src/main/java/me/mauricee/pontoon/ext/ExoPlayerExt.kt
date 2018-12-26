package me.mauricee.pontoon.ext

import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

inline fun ExoPlayer.prepareAnd(source: MediaSource, crossinline action: (player: ExoPlayer) -> Unit) {
    this.addListener(object : Player.EventListener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

        override fun onSeekProcessed() {}

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

        override fun onPlayerError(error: ExoPlaybackException?) {}

        override fun onLoadingChanged(isLoading: Boolean) {}

        override fun onPositionDiscontinuity(reason: Int) {}

        override fun onRepeatModeChanged(repeatMode: Int) {}

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                action(this@prepareAnd)
                removeListener(this)
            }
        }
    })
    prepare(source)
}