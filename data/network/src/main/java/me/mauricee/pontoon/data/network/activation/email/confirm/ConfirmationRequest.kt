package me.mauricee.pontoon.data.network.activation.email.confirm

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfirmationRequest(@Json(name = "code") val code: String, @Json(name = "username") val username: String)