package me.mauricee.pontoon.rx.media

import androidx.media.AudioAttributesCompat
import androidx.media2.common.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

fun SessionPlayer.playerCallbackEvents(executor: Executor = Executors.newSingleThreadExecutor()): Observable<SessionPlayerEvent> {
    return PlayerCallbackObservable(executor, this)
}

private class PlayerCallbackObservable(private val executor: Executor, private val sessionPlayer: SessionPlayer) : Observable<SessionPlayerEvent>() {

    override fun subscribeActual(observer: Observer<in SessionPlayerEvent>) {
        val listener = Listener(observer, sessionPlayer).also(observer::onSubscribe)
        sessionPlayer.registerPlayerCallback(executor, listener)
    }

    private class Listener(private val observer: Observer<in SessionPlayerEvent>,
                           private val sessionPlayer: SessionPlayer) : SessionPlayer.PlayerCallback(), Disposable {

        private val isDisposed: AtomicBoolean = AtomicBoolean(false)

        override fun onPlayerStateChanged(player: SessionPlayer, playerState: Int) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.PlayerStateChangedEvent(player, playerState))
            }
        }

        override fun onBufferingStateChanged(player: SessionPlayer, item: MediaItem?, buffState: Int) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.BufferingStateChangedEvent(player, item, buffState))
            }
        }

        override fun onPlaybackSpeedChanged(player: SessionPlayer, playbackSpeed: Float) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.PlaybackSpeedChangedEvent(player, playbackSpeed))
            }
        }

        override fun onSeekCompleted(player: SessionPlayer, position: Long) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.SeekCompletedEvent(player, position))
            }
        }

        override fun onPlaylistChanged(player: SessionPlayer, list: MutableList<MediaItem>?, metadata: MediaMetadata?) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.PlaylistChangedEvent(player, list, metadata))
            }
        }

        override fun onPlaylistMetadataChanged(player: SessionPlayer, metadata: MediaMetadata?) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.PlaylistMetadataChangedEvent(player, metadata))
            }
        }

        override fun onShuffleModeChanged(player: SessionPlayer, shuffleMode: Int) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.ShuffleModeChangedEvent(player, shuffleMode))
            }
        }

        override fun onRepeatModeChanged(player: SessionPlayer, repeatMode: Int) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.RepeatModeChangedEvent(player, repeatMode))
            }
        }

        override fun onCurrentMediaItemChanged(player: SessionPlayer, item: MediaItem) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.CurrentMediaItemChangedEvent(player, item))
            }
        }

        override fun onPlaybackCompleted(player: SessionPlayer) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.PlaybackCompletedEvent(player))
            }
        }

        override fun onAudioAttributesChanged(player: SessionPlayer, attributes: AudioAttributesCompat?) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.AudioAttributesChangedEvent(player, attributes))
            }
        }

        override fun onVideoSizeChanged(player: SessionPlayer, size: VideoSize) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.VideoSizeChangedEvent(player, size))
            }
        }

        override fun onSubtitleData(player: SessionPlayer, item: MediaItem, track: SessionPlayer.TrackInfo, data: SubtitleData) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.SubtitleDataEvent(player, item, track, data))
            }
        }

        override fun onTracksChanged(player: SessionPlayer, tracks: MutableList<SessionPlayer.TrackInfo>) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.TracksChangedEvent(player, tracks))
            }
        }

        override fun onTrackSelected(player: SessionPlayer, trackInfo: SessionPlayer.TrackInfo) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.TrackSelectedEvent(player, trackInfo))
            }
        }

        override fun onTrackDeselected(player: SessionPlayer, trackInfo: SessionPlayer.TrackInfo) {
            if (!isDisposed()) {
                observer.onNext(SessionPlayerEvent.TrackDeselectedEvent(player, trackInfo))
            }
        }

        override fun dispose() {
            sessionPlayer.unregisterPlayerCallback(this)
            isDisposed.set(true)
        }

        override fun isDisposed(): Boolean = isDisposed.get()
    }
}

sealed class SessionPlayerEvent {
    data class PlayerStateChangedEvent(val player: SessionPlayer, val playerState: Int) : SessionPlayerEvent()
    data class BufferingStateChangedEvent(val player: SessionPlayer, val item: MediaItem?, val buffState: Int) : SessionPlayerEvent()
    data class PlaybackSpeedChangedEvent(val player: SessionPlayer, val playbackSpeed: Float) : SessionPlayerEvent()
    data class SeekCompletedEvent(val player: SessionPlayer, val position: Long) : SessionPlayerEvent()
    data class PlaylistChangedEvent(val player: SessionPlayer, val list: MutableList<MediaItem>?, val metadata: MediaMetadata?) : SessionPlayerEvent()
    data class PlaylistMetadataChangedEvent(val player: SessionPlayer, val metadata: MediaMetadata?) : SessionPlayerEvent()
    data class ShuffleModeChangedEvent(val player: SessionPlayer, val shuffleMode: Int) : SessionPlayerEvent()
    data class RepeatModeChangedEvent(val player: SessionPlayer, val repeatMode: Int) : SessionPlayerEvent()
    data class CurrentMediaItemChangedEvent(val player: SessionPlayer, val item: MediaItem) : SessionPlayerEvent()
    data class PlaybackCompletedEvent(val player: SessionPlayer) : SessionPlayerEvent()
    data class AudioAttributesChangedEvent(val player: SessionPlayer, val attributes: AudioAttributesCompat?) : SessionPlayerEvent()
    data class VideoSizeChangedEvent(val player: SessionPlayer, val size: VideoSize) : SessionPlayerEvent()
    data class SubtitleDataEvent(val player: SessionPlayer, val item: MediaItem, val track: SessionPlayer.TrackInfo, val data: SubtitleData) : SessionPlayerEvent()
    data class TracksChangedEvent(val player: SessionPlayer, val tracks: MutableList<SessionPlayer.TrackInfo>) : SessionPlayerEvent()
    data class TrackSelectedEvent(val player: SessionPlayer, val trackInfo: SessionPlayer.TrackInfo) : SessionPlayerEvent()
    data class TrackDeselectedEvent(val player: SessionPlayer, val trackInfo: SessionPlayer.TrackInfo) : SessionPlayerEvent()
}