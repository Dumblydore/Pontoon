package me.mauricee.pontoon.common.playback

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.gms.cast.framework.SessionManager
import io.reactivex.Observable
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

interface Playback {

    val bufferedPosition: Long

    @PlaybackStateCompat.State
    val playerState: Observable<Int>

    var position: Long

    fun pause()

    fun play()

    fun stop()

    fun prepare(mediaItem: MediaItem)

    data class MediaItem(val source: String, val video: Video, val position: Long = 0L)

    class Factory @Inject constructor(private val hlsSource: HlsMediaSource.Factory, private val castSessionManager: SessionManager, private val context: Context) {

        fun initalPlayback() = createLocalPlayback()

        private fun createLocalPlayback(): Playback {
            return LocalPlayback(hlsSource, context)
        }

        private fun createCastPlayback(): Playback {
            return CastPlayback(castSessionManager.currentCastSession)
        }

        fun switchPlayback(currentPlayback: Playback, itemToPlay: MediaItem?): Playback {
            val newPlayback = if (currentPlayback is LocalPlayback) createCastPlayback() else createLocalPlayback()
            itemToPlay?.let { newPlayback.prepare(it) }
            return newPlayback
        }
    }

}