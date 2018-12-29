package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName

data class ConfirmationRequest(@SerializedName("code") val code: String, @SerializedName("username") val username: String)