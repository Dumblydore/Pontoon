package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json

data class ConfirmationRequest(@Json(name = "code") val code: String, @Json(name = "username") val username: String)