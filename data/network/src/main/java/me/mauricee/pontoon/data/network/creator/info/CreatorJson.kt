package me.mauricee.pontoon.data.network.creator.info

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import me.mauricee.pontoon.data.network.common.Image

@JsonClass(generateAdapter=true)
data class CreatorJson(@Json(name ="about") val about: String,
                       @Json(name ="cover") val cover: Image,
                       @Json(name ="description") val description: String,
                       @Json(name ="id") val id: String,
                       @Json(name ="owner") val owner: String,
                       @Json(name ="title") val title: String,
                       @Json(name ="urlname") val urlname: String
)