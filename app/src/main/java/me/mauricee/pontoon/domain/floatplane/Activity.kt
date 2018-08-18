package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

class Activity(@SerializedName("comment") val comment: String,
               @SerializedName("date") val date: Instant,
               @SerializedName("video") val video: Video,
               @SerializedName("visibility") val visibility: String) {

    class Video(@SerializedName("title") val title: String,
                @SerializedName("GUID") val id: String,
                @SerializedName("creator") val creator: Creator)

    class Creator(@SerializedName("title") val title: String, @SerializedName("url") val url: String)

    class Response(@SerializedName("activity") val activity: List<Activity>)
}