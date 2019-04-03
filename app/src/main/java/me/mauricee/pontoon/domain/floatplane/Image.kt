package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class Image(@Json(name = "height") val height: Int,
                 @Json(name = "path") val path: String,
                 @Json(name = "width") val width: Int,
                 @Json(name = "childImages") val childImages: List<Image>)