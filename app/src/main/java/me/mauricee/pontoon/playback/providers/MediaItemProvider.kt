package me.mauricee.pontoon.playback.providers

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.UriMediaItem
import androidx.media2.session.MediaSession
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import me.mauricee.pontoon.model.session.PlaybackQuality
import me.mauricee.pontoon.model.video.Stream
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.playback.PontoonMetadata
import me.mauricee.pontoon.rx.RxTuple
import javax.inject.Inject

class MediaItemProvider @Inject constructor(private val videoRepository: VideoRepository) : SessionCallbackBuilder.MediaItemProvider {
    override fun onCreateMediaItem(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, mediaId: String): MediaItem {
        return RxTuple.zipAsPair(videoRepository.getVideo(mediaId).firstOrError(), videoRepository.getStream(mediaId))
                .map {
                    val (video, streams) = it
                    UriMediaItem.Builder(streams.first().url.toUri())
                            .setStartPosition(0)
                            .setEndPosition(video.entity.duration * 1000L)
                            .setMetadata(toMediaMetaData(video, streams))
                            .build()
                }.blockingGet()
    }

    @SuppressLint("WrongConstant")
    private fun toMediaMetaData(video: Video, streams: List<Stream>): MediaMetadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, video.id)
            .putString(MediaMetadata.METADATA_KEY_ART_URI, video.entity.thumbnail)
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, video.entity.title)
            .putText(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, video.entity.description)
            .putText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, video.creator.entity.name)
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, video.entity.thumbnail)
            .putString(MediaMetadata.METADATA_KEY_TITLE, video.entity.title)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, video.entity.duration * 1000L)
            .putString(PontoonMetadata.TimeLineUri, "https://cms.linustechtips.com/get/sprite/by_guid/${video.id}")
            .setExtras(Bundle().apply {
                putParcelableArrayList("test", ArrayList(streams.sortedBy { it.ordinal }.map { NewPlayer.Quality(it.name, it.url) }))
            }).build()

}