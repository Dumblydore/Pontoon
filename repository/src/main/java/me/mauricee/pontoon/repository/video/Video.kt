package me.mauricee.pontoon.repository.video

import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.data.local.video.VideoCreatorJoin
import me.mauricee.pontoon.data.local.video.VideoEntity
import me.mauricee.pontoon.data.network.video.VideoJson
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.creator.toModel
import org.threeten.bp.Instant

data class Video(override val id: String,
                 val creator: Creator,
                 val description: String,
                 val releaseDate: Instant,
                 val duration: Long,
                 val thumbnail: String,
                 val title: String,
                 val watched: Instant?) : Diffable<String> {

    fun toBrowsableUrl(): String = "https://www.floatplane.com/video/$id"
}


fun VideoJson.toEntity(): VideoEntity {
    return VideoEntity(
            guid,
            creator,
            description,
            releaseDate ?: Instant.now(),
            duration,
            thumbnail?.path ?: "",
            title,
            null)
}

fun VideoCreatorJoin.toModel(): Video {
    return Video(entity.id,
            creator.toModel(),
            entity.description,
            entity.releaseDate,
            entity.duration,
            entity.thumbnail,
            entity.title,
            entity.watched)
}