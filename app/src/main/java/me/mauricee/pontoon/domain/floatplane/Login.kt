package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(@Json(name = "username") val username: String, @Json(name = "password") val password: String)

@JsonClass(generateAdapter = true)
data class LoginResponse(@Json(name = "message") val message: String, @Json(name = "user") val user: User)

@JsonClass(generateAdapter = true)
data class LoginAuthToken(@Json(name = "token") val token: String)