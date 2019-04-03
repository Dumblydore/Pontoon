package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Edge(@Json(name = "allowStreaming") val allowStreaming: Boolean,
                @Json(name = "allowDownload") val allowDownload: Boolean,
                @Json(name = "hostname") val hostname: String) {

    @JsonClass(generateAdapter = true)
    data class Response(@Json(name = "edges") val edges: List<Edge>)
}