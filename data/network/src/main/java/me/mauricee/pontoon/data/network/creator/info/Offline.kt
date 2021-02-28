package me.mauricee.pontoon.data.network.creator.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import me.mauricee.pontoon.data.network.common.Image

@JsonClass(generateAdapter = true)
data class Offline(
    @Json(name = "description")
    val description: String,
    @Json(name = "thumbnail")
    val thumbnail: Image,
    @Json(name = "title")
    val title: String
)