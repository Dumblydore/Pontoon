package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.squareup.moshi.Json
import org.threeten.bp.Instant


@Keep
data class Activity(@Json(name = "comment") val comment: String,
                    @Json(name = "time") val date: Instant,
                    @Json(name = "video") val video: Video) {

    @Keep
    data class Video(@Json(name = "title") val title: String,
                     @Json(name = "GUID") val id: String,
                     @Json(name = "creator") val creator: Creator)

    @Keep
    data class Creator(@Json(name = "title") val title: String, @Json(name = "url") val url: String)

    @Keep
    data class Response(@Json(name = "activity") val activity: List<Activity>, @Json(name = "visibility") val visibility: String)
}