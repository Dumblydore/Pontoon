package me.mauricee.pontoon.data.network.creator.livestream

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Emote(
        @Json(name = "code") val code: String,
        @Json(name = "image") val image: String
)