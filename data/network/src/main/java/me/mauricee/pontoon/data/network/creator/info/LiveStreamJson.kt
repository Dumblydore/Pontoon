package me.mauricee.pontoon.data.network.creator.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.mauricee.pontoon.data.network.common.Image

@JsonClass(generateAdapter = true)
data class LiveStreamJson(
        @Json(name = "description")
        val description: String,
        @Json(name = "id")
        val id: String,
        @Json(name = "offline")
        val offline: Offline,
        @Json(name = "owner")
        val owner: String,
        @Json(name = "streamPath")
        val streamPath: String,
        @Json(name = "thumbnail")
        val thumbnail: Image,
        @Json(name = "title")
        val title: String
)