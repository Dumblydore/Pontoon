package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant


@Keep
data class Activity(@SerializedName("comment") val comment: String,
                    @SerializedName("time") val date: Instant,
                    @SerializedName("video") val video: Video) {

    @Keep
    data class Video(@SerializedName("title") val title: String,
                     @SerializedName("GUID") val id: String,
                     @SerializedName("creator") val creator: Creator)

    @Keep
    data class Creator(@SerializedName("title") val title: String, @SerializedName("url") val url: String)

    @Keep
    data class Response(@SerializedName("activity") val activity: List<Activity>, @SerializedName("visibility") val visibility: String)
}