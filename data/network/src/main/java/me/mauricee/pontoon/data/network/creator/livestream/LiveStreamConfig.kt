package me.mauricee.pontoon.data.network.creator.livestream

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LiveStreamConfig(
        @Json(name = "pingInterval") val pingInterval: Int,
        @Json(name = "pingTimeout") val pingTimeout: Int,
        @Json(name = "sid") val sid: String,
        @Json(name = "upgrades") val upgrades: List<String>
)