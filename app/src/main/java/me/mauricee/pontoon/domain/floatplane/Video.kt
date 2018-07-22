package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

data class Video(@SerializedName("creator") val creator: String,
                 @SerializedName("description") val description: String,
                 @SerializedName("duration") val duration: Long,
                 @SerializedName("guid") val guid: String,
                 @SerializedName("private") val isPrivate: Boolean,
                 @SerializedName("releaseDate") val releaseDate: Instant,
                 @SerializedName("tags") val tags: List<String>,
                 @SerializedName("thumbnail") val thumbnail: Image?,
                 @SerializedName("title") val title: String) {
    val defaultThumbnail: String
        get() = thumbnail?.path ?: ""
}