package me.mauricee.pontoon.common.playback

import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import com.google.android.gms.cast.Cast
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable

class CastPlayback(private val session: CastSession) : Playback, Cast.Listener() {

    private val remoteMediaClient: RemoteMediaClient by lazy { session.remoteMediaClient }
    private val playbackState: Relay<Int> = BehaviorRelay.create()
    override val location: PlaybackLocation
        get() = PlaybackLocation.Remote
    override val bufferedPosition: Long
        get() = 0L
    override val playerState: Observable<Int>
        get() = playbackState.hide()
    override var position: Long
        get() = remoteMediaClient.approximateStreamPosition
        set(value) {
            remoteMediaClient.seek(value)
        }

    init {
        session.addCastListener(this)
        remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onPreloadStatusUpdated() {
                super.onPreloadStatusUpdated()
                playbackState.accept(PlaybackStateCompat.STATE_CONNECTING)
            }

            override fun onStatusUpdated() {
                super.onStatusUpdated()
                val newState = when {
                    remoteMediaClient.isPlaying -> PlaybackStateCompat.STATE_PLAYING
                    remoteMediaClient.isPaused -> PlaybackStateCompat.STATE_PAUSED
                    remoteMediaClient.isBuffering -> PlaybackStateCompat.STATE_BUFFERING
                    else -> PlaybackStateCompat.STATE_NONE
                }
                playbackState.accept(newState)
            }
        })
    }

    override fun pause() {
        remoteMediaClient.pause()
    }

    override fun play() {
        remoteMediaClient.play()
    }

    override fun stop() {
//        remoteMediaClient.stop()
//        session.removeCastListener(this)
    }

    override fun prepare(mediaItem: Playback.MediaItem, playOnPrepare: Boolean) {
        val video = mediaItem.video
        val metaData = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, video.title)
            putString(MediaMetadata.KEY_SUBTITLE, video.creator.name)
            addImage(WebImage(mediaItem.video.thumbnail.toUri()))
        }
        val info = MediaInfo.Builder(mediaItem.source)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("application/x-mpegurl")
                .setMetadata(metaData)
//                .setStreamDuration(1000)
//                .setMediaTracks(listOf(MediaTrack.Builder()))
                .build()
        val options = MediaLoadOptions.Builder()
                .setAutoplay(true)
                .setPlayPosition(0)
                .build()
        remoteMediaClient.load(info, options)
    }


}