package me.mauricee.pontoon.playback

import android.os.Looper
import android.view.TextureView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.video.VideoListener
import me.mauricee.pontoon.common.playback.PlayerFactory
import javax.inject.Inject

class WrappedExoPlayer @Inject constructor(localExoPlayer: SimpleExoPlayer, private val playerFactory: PlayerFactory) : Player {
    private val disposable = playerFactory.playback.subscribe { activePlayer = it }

    var activePlayer: Player = localExoPlayer
        private set


    fun addVideoListener(listener: VideoListener) {
        (activePlayer as? SimpleExoPlayer)?.addVideoListener(listener)
    }

    fun removeVideoListener(listener: VideoListener) {
        (activePlayer as? SimpleExoPlayer)?.removeVideoListener(listener)
    }

    fun setVideoTextureView(textureView: TextureView) {
        (activePlayer as? SimpleExoPlayer)?.setVideoTextureView(textureView)
    }

    override fun getAudioComponent(): Player.AudioComponent? = activePlayer.audioComponent

    override fun getVideoComponent(): Player.VideoComponent? = activePlayer.videoComponent

    override fun getTextComponent(): Player.TextComponent? = activePlayer.textComponent

    override fun getMetadataComponent(): Player.MetadataComponent? = activePlayer.metadataComponent

    override fun getDeviceComponent(): Player.DeviceComponent? = activePlayer.deviceComponent

    override fun getApplicationLooper(): Looper = activePlayer.applicationLooper

    override fun addListener(listener: Player.EventListener) = activePlayer.addListener(listener)

    override fun removeListener(listener: Player.EventListener) = activePlayer.removeListener(listener)

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) = activePlayer.setMediaItems(mediaItems)

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) = activePlayer.setMediaItems(mediaItems, resetPosition)

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, startWindowIndex: Int, startPositionMs: Long) = activePlayer.setMediaItems(mediaItems, startWindowIndex, startPositionMs)

    override fun setMediaItem(mediaItem: MediaItem) = activePlayer.setMediaItem(mediaItem)

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) = activePlayer.setMediaItem(mediaItem, startPositionMs)

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) = activePlayer.setMediaItem(mediaItem, resetPosition)

    override fun addMediaItem(mediaItem: MediaItem) = activePlayer.addMediaItem(mediaItem)

    override fun addMediaItem(index: Int, mediaItem: MediaItem) = activePlayer.addMediaItem(index, mediaItem)

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) = activePlayer.addMediaItems(mediaItems)

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) = activePlayer.addMediaItems(index, mediaItems)

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) = activePlayer.moveMediaItem(currentIndex, newIndex)

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) = activePlayer.moveMediaItems(fromIndex, toIndex, newIndex)

    override fun removeMediaItem(index: Int) = activePlayer.removeMediaItem(index)

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) = activePlayer.removeMediaItems(fromIndex, toIndex)

    override fun clearMediaItems() = activePlayer.clearMediaItems()

    override fun prepare() = activePlayer.prepare()

    override fun getPlaybackState(): Int = activePlayer.playbackState

    override fun getPlaybackSuppressionReason(): Int = activePlayer.playbackSuppressionReason

    override fun isPlaying(): Boolean = activePlayer.isPlaying

    override fun getPlayerError(): ExoPlaybackException? = activePlayer.playerError

    override fun getPlaybackError(): ExoPlaybackException? = activePlayer.playbackError

    override fun play() = activePlayer.play()

    override fun pause() = activePlayer.pause()

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        activePlayer.playWhenReady = playWhenReady
    }

    override fun getPlayWhenReady(): Boolean = activePlayer.playWhenReady

    override fun setRepeatMode(repeatMode: Int) {
        activePlayer.repeatMode = repeatMode
    }

    override fun getRepeatMode(): Int = activePlayer.repeatMode

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) = activePlayer.setShuffleModeEnabled(shuffleModeEnabled)

    override fun getShuffleModeEnabled(): Boolean = activePlayer.shuffleModeEnabled

    override fun isLoading(): Boolean = activePlayer.isLoading

    override fun seekToDefaultPosition() = activePlayer.seekToDefaultPosition()

    override fun seekToDefaultPosition(windowIndex: Int) = activePlayer.seekToDefaultPosition(windowIndex)

    override fun seekTo(positionMs: Long) = activePlayer.seekTo(positionMs)

    override fun seekTo(windowIndex: Int, positionMs: Long) = activePlayer.seekTo(windowIndex, positionMs)

    override fun hasPrevious(): Boolean = activePlayer.hasPrevious()

    override fun previous() = activePlayer.previous()

    override fun hasNext(): Boolean = activePlayer.hasNext()

    override fun next() = activePlayer.next()

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters?) = activePlayer.setPlaybackParameters(playbackParameters)

    override fun getPlaybackParameters(): PlaybackParameters = activePlayer.playbackParameters

    override fun stop() = activePlayer.stop()

    override fun stop(reset: Boolean) = activePlayer.stop(reset)

    override fun release() = activePlayer.release()

    override fun getRendererCount(): Int = activePlayer.rendererCount

    override fun getRendererType(index: Int): Int = activePlayer.getRendererType(index)

    override fun getTrackSelector(): TrackSelector? = activePlayer.trackSelector

    override fun getCurrentTrackGroups(): TrackGroupArray = activePlayer.currentTrackGroups

    override fun getCurrentTrackSelections(): TrackSelectionArray = activePlayer.currentTrackSelections

    override fun getCurrentManifest(): Any? = activePlayer.currentManifest

    override fun getCurrentTimeline(): Timeline = activePlayer.currentTimeline

    override fun getCurrentPeriodIndex(): Int = activePlayer.currentPeriodIndex

    override fun getCurrentWindowIndex(): Int = activePlayer.currentWindowIndex

    override fun getNextWindowIndex(): Int = activePlayer.nextWindowIndex

    override fun getPreviousWindowIndex(): Int = activePlayer.previousWindowIndex

    override fun getCurrentTag(): Any? = activePlayer.currentTag

    override fun getCurrentMediaItem(): MediaItem? = activePlayer.currentMediaItem

    override fun getMediaItemCount(): Int = activePlayer.mediaItemCount

    override fun getMediaItemAt(index: Int): MediaItem = activePlayer.getMediaItemAt(index)

    override fun getDuration(): Long = activePlayer.duration

    override fun getCurrentPosition(): Long = activePlayer.currentPosition

    override fun getBufferedPosition(): Long = activePlayer.bufferedPosition

    override fun getBufferedPercentage(): Int = activePlayer.bufferedPercentage

    override fun getTotalBufferedDuration(): Long = activePlayer.totalBufferedDuration

    override fun isCurrentWindowDynamic(): Boolean = activePlayer.isCurrentWindowDynamic

    override fun isCurrentWindowLive(): Boolean = activePlayer.isCurrentWindowLive

    override fun getCurrentLiveOffset(): Long = activePlayer.currentLiveOffset

    override fun isCurrentWindowSeekable(): Boolean = activePlayer.isCurrentWindowSeekable

    override fun isPlayingAd(): Boolean = activePlayer.isPlayingAd

    override fun getCurrentAdGroupIndex(): Int = activePlayer.currentAdGroupIndex

    override fun getCurrentAdIndexInAdGroup(): Int = activePlayer.currentAdIndexInAdGroup

    override fun getContentDuration(): Long = activePlayer.contentDuration

    override fun getContentPosition(): Long = activePlayer.contentPosition

    override fun getContentBufferedPosition(): Long = activePlayer.contentBufferedPosition
}