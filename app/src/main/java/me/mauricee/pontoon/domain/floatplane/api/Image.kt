package me.mauricee.pontoon.domain.floatplane.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Image(@SerializedName("height") val height: Int,
                 @SerializedName("path") val path: String,
                 @SerializedName("width") val width: Int,
                 @SerializedName("childImages") val childImages: List<Image>)