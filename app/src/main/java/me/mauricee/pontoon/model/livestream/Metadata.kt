package me.mauricee.pontoon.model.livestream

import com.google.gson.annotations.SerializedName

data class Metadata(@SerializedName("sid") val sid: String,
                    @SerializedName("pingInterval") val pingInterval: Long,
                    @SerializedName("pingTimeout") val pingTimeout: Long)