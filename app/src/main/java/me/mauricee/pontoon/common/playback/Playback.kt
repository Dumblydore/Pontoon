package me.mauricee.pontoon.common.playback

import android.content.Context
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import io.reactivex.Observable
import me.mauricee.pontoon.ext.toObservable
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.rx.cast.SessionEvent
import me.mauricee.pontoon.rx.cast.events
import javax.inject.Inject

interface Playback {

    val bufferedPosition: Long

    val location: PlaybackLocation

    val playerState: Observable<Int>

    var position: Long

    fun pause()

    fun play()

    fun stop()

    fun prepare(mediaItem: MediaItem)

    //    fun release()
    data class MediaItem(val source: String, val video: Video, val position: Long = 0L)

    class Factory @Inject constructor(private val hlsSource: HlsMediaSource.Factory, private val castSessionManager: SessionManager, private val context: Context) {

        val playback: Observable<Playback>
            get() = castSessionManager.events().flatMap {
                when {
                    it.isConnected() -> createCastPlayback((it as SessionEvent.ConnectedEvent).castSession).toObservable()
                    it.isDisconnected() -> createLocalPlayback().toObservable()
                    else -> Observable.empty()
                }
            }

        val initialPlayback: Playback
            get() = if (checkForConnection())
                createCastPlayback(castSessionManager.currentCastSession)
            else
                createLocalPlayback()

        private fun createLocalPlayback(): Playback = LocalPlayback(hlsSource, context)

        private fun createCastPlayback(castSession: CastSession): Playback = CastPlayback(castSession)

        private fun checkForConnection() = castSessionManager.currentCastSession?.let {
            castSessionManager.currentCastSession.isConnected || castSessionManager.currentCastSession.isConnecting
        } ?: false
    }
}
