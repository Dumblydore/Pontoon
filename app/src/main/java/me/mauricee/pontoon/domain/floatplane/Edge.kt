package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName

data class Edge(@SerializedName("allowStreaming") val allowStreaming: Boolean,
                @SerializedName("allowDownloads") val allowDownloads: Boolean,
                @SerializedName("hostname") val hostname: String) {

    data class Response(@SerializedName("edges") val edges: List<Edge>)
}