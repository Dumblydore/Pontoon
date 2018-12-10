package me.mauricee.pontoon.common.playback

import androidx.core.net.toUri
import com.google.android.gms.cast.Cast
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.model.video.Video

class CastPlayback(private val session: CastSession) : Playback, Cast.Listener() {

    private val remoteMediaClient: RemoteMediaClient = session.remoteMediaClient
    private val relay: Relay<Int> = PublishRelay.create()
    override val bufferedPosition: Long
        get() = 0L
    override val playerState: Observable<Int>
        get() = relay.hide()
    override var position: Long
        get() = remoteMediaClient.approximateStreamPosition
        set(value) {
            remoteMediaClient.seek(value)
        }

    init {
        session.addCastListener(this)
    }

    override fun pause() {
        remoteMediaClient.pause()
    }

    override fun play() {
        remoteMediaClient.play()
    }

    override fun stop() {
        remoteMediaClient.stop()
        session.removeCastListener(this)
    }

    override fun prepare(mediaItem: Playback.MediaItem) {
        val video = mediaItem.video
        val metaData = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, video.title)
            putString(MediaMetadata.KEY_SUBTITLE, video.creator.name)
            addImage(WebImage(mediaItem.video.thumbnail.toUri()))
        }
        val info = MediaInfo.Builder("")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(metaData)
                .setStreamDuration(video.duration)
                .build()
        val options = MediaLoadOptions.Builder()
                .setAutoplay(true)
                .setPlayPosition(mediaItem.position)
                .build()
        remoteMediaClient.load(info, options)
    }


}