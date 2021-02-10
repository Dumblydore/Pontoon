package me.mauricee.pontoon.playback.providers

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.UriMediaItem
import androidx.media2.session.MediaSession
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import dagger.hilt.android.scopes.ActivityRetainedScoped
import me.mauricee.pontoon.model.video.Stream
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.model.video.VideoRepository
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.playback.PontoonMetadata
import me.mauricee.pontoon.rx.RxTuple
import javax.inject.Inject

@ActivityRetainedScoped
class MediaItemProvider @Inject constructor(private val videoRepository: VideoRepository) : SessionCallbackBuilder.MediaItemProvider {
    override fun onCreateMediaItem(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, mediaId: String): MediaItem {
        return if (mediaId.isEmpty())
            MediaItem.Builder().build()
        else {
            val (id, quality) = mediaId.split(":")
            RxTuple.zipAsPair(videoRepository.getVideo(id).firstOrError(), videoRepository.getStream(id))
                    .map {
                        val (video, streams) = it
                        val defaultStream = getDefaultQualityLevel(quality, streams)
                        UriMediaItem.Builder(defaultStream.url.toUri())
                                .setStartPosition(0)
                                .setEndPosition(video.entity.duration * 1000L)
                                .setMetadata(toMediaMetaData(mediaId, video, defaultStream, streams))
                                .build()
                    }.blockingGet()
        }
    }


    private fun getDefaultQualityLevel(qualityLevel: String, streams: List<Stream>) = streams.firstOrNull {
        it.name == qualityLevel
    } ?: streams.first()

    private fun toMediaMetaData(mediaId: String, video: Video, defaultStream: Stream, streams: List<Stream>): MediaMetadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId)
            .putString(MediaMetadata.METADATA_KEY_ART_URI, video.entity.thumbnail)
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, video.entity.title)
            .putText(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, video.entity.description)
            .putText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, video.creator.entity.name)
            .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, video.entity.thumbnail)
            .putString(MediaMetadata.METADATA_KEY_TITLE, video.entity.title)
            .putLong(MediaMetadata.METADATA_KEY_DURATION, video.entity.duration * 1000L)
            .setExtras(Bundle().apply {
                putString(PontoonMetadata.TimeLineUri, "https://cms.linustechtips.com/get/sprite/by_guid/${video.id}")
                putParcelable(PontoonMetadata.CurrentQualityLevelKey, Player.Quality(defaultStream.name, defaultStream.url))
                putParcelableArrayList(PontoonMetadata.QualityLevelsKey, ArrayList(streams.sortedBy { it.ordinal }.map { Player.Quality(it.name, it.url) }))
            }).build()

}