package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Creator(@Json(name = "about") val about: String,
        @Json(name = "cover") val cover: Image,
        @Json(name = "description") val description: String,
        @Json(name = "id") val id: String,
        @Json(name = "owner") val owner: String,
        @Json(name = "title") val title: String,
        @Json(name = "urlname") val urlname: String
)