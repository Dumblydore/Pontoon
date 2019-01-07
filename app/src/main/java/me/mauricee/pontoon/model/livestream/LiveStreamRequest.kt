package me.mauricee.pontoon.model.livestream

import com.google.gson.annotations.SerializedName

data class ChatRequest(@SerializedName("channel") val channel: String, @SerializedName("message") val message: String)
data class UserListRequest(@SerializedName("channel") val channel: String, @SerializedName("message") val message: String)

