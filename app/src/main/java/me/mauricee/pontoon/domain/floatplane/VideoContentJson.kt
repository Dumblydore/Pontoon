package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName

data class VideoContentJson(@SerializedName("cdn") val cdn: String,
                            @SerializedName("resource") val resource: Resource) {

    data class Resource(@SerializedName("uri") val uri: String,
                        @SerializedName("data") val data: Data)

    data class Data(@SerializedName("qualityLevelParams") val qualityLevelParams: Map<String, Token>,
                    @SerializedName("qualityLevels") val qualityLevels: List<QualityLevel>)

    data class Token(@SerializedName("token") val token: String)

    data class QualityLevel(@SerializedName("name") val name: String,
                            @SerializedName("width") val width: Int,
                            @SerializedName("height") val height: Int,
                            @SerializedName("label") val label: String,
                            @SerializedName("order") val order: Int)

    companion object {

    }
}

enum class ContentType {
    @SerializedName("vod")
    vod,
    @SerializedName("download")
    download
}