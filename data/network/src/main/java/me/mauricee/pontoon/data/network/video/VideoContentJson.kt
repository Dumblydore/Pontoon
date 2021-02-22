package me.mauricee.pontoon.data.network

import com.squareup.moshi.Json

data class VideoContentJson(@Json(name ="cdn") val cdn: String,
                            @Json(name ="resource") val resource: Resource) {

    data class Resource(@Json(name ="uri") val uri: String,
                        @Json(name ="data") val data: Data)

    data class Data(@Json(name ="qualityLevelParams") val qualityLevelParams: Map<String, Token>,
                    @Json(name ="qualityLevels") val qualityLevels: List<QualityLevel>)

    data class Token(@Json(name ="token") val token: String)

    data class QualityLevel(@Json(name ="name") val name: String,
                            @Json(name ="width") val width: Int,
                            @Json(name ="height") val height: Int,
                            @Json(name ="label") val label: String,
                            @Json(name ="order") val order: Int)

    companion object {

    }
}

enum class ContentType {
    @Json(name ="vod")
    vod,
    @Json(name ="download")
    download
}