package me.mauricee.pontoon.data.network.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter=true)
data class Image(@Json(name ="height") val height: Int,
                 @Json(name ="path") val path: String,
                 @Json(name ="width") val width: Int,
                 @Json(name ="childImages") val childImages: List<Image>?)