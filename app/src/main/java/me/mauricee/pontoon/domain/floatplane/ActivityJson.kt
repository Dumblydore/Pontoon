package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant


@Keep
data class ActivityJson(@SerializedName("comment") val comment: String,
                        @SerializedName("time") val date: Instant,
                        @SerializedName("postId") val postId: String) {

    @Keep
    data class Video(@SerializedName("title") val title: String,
                     @SerializedName("GUID") val id: String,
                     @SerializedName("creator") val creator: Creator)

    @Keep
    data class Creator(@SerializedName("title") val title: String, @SerializedName("url") val url: String)

    @Keep
    data class Response(@SerializedName("activity") val activity: List<ActivityJson>, @SerializedName("visibility") val visibility: String)
}