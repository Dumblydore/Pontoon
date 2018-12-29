package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName

data class LiveStreamMetadata(@SerializedName("id") val id: String,
                              @SerializedName("description") val description: String,
                              @SerializedName("offline") val offline: Offline,
                              @SerializedName("owner") val owner: String,
                              @SerializedName("streamPath") val streamPath: String,
                              @SerializedName("title") val title: String) {

    data class Offline(@SerializedName("title") val title: String,
                       @SerializedName("description") val description: String,
                       @SerializedName("thumbnail") val thumbnail: Image
    )
}
