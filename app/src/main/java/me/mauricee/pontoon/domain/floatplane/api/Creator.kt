package me.mauricee.pontoon.domain.floatplane.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Creator(@SerializedName("about") val about: String,
                   @SerializedName("cover") val cover: Image,
                   @SerializedName("description") val description: String,
                   @SerializedName("id") val id: String,
                   @SerializedName("owner") val owner: String,
                   @SerializedName("title") val title: String,
                   @SerializedName("urlname") val urlname: String
)