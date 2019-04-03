package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.mauricee.pontoon.model.video.VideoEntity
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class Video(@Json(name = "creator") val creator: String,
                 @Json(name = "description") val description: String,
                 @Json(name = "duration") val duration: Long,
                 @Json(name = "guid") val guid: String,
                 @Json(name = "private") val isPrivate: Boolean,
                 @Json(name = "releaseDate") val releaseDate: Instant,
                 @Json(name = "tags") val tags: List<String>,
                 @Json(name = "thumbnail") val thumbnail: Image?,
                 @Json(name = "title") val title: String) {
    val defaultThumbnail: String
        get() = thumbnail?.path ?: ""

    fun toEntity() = VideoEntity(guid, creator, description, releaseDate, duration, defaultThumbnail, title, null)
}