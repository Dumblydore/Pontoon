package me.mauricee.pontoon.data.network.creator.livestream

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RadioChatterJson(
        @Json(name = "channel") val channel: String,
        @Json(name = "emotes") val emotes: List<Emote>,
        @Json(name = "id") val id: String,
        @Json(name = "message") val message: String,
        @Json(name = "success") val success: Boolean,
        @Json(name = "userGUID") val userGUID: String,
        @Json(name = "userType") val userType: String,
        @Json(name = "username") val username: String
)