package me.mauricee.pontoon.repository.creator

data class LiveStream(
        val liveStreamTitle: String,
        val liveStreamDescription: String,
        val liveStreamThumbnail: String,
        val liveStreamPath: String,
        val offlineImage: String
)