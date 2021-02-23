package me.mauricee.pontoon.data.network.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class ActivityJson(@Json(name = "comment") val comment: String,
                        @Json(name = "time") val date: Instant,
                        @Json(name = "postId") val postId: String?) {

    @JsonClass(generateAdapter = true)
    data class Video(@Json(name = "title") val title: String,
                     @Json(name = "GUID") val id: String,
                     @Json(name = "creator") val creator: Creator)

    @JsonClass(generateAdapter = true)
    data class Creator(@Json(name = "title") val title: String, @Json(name = "url") val url: String)

    @JsonClass(generateAdapter = true)
    data class Response(@Json(name = "activity") val activity: List<ActivityJson>,
                        @Json(name = "visibility") val visibility: String)
}