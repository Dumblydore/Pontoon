package me.mauricee.pontoon.data.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import me.mauricee.pontoon.data.network.user.UserJson

@JsonClass(generateAdapter=true)
data class LoginRequest(@Json(name ="username") val username: String, @Json(name ="password") val password: String)

@JsonClass(generateAdapter=true)
data class LoginResponse(@Json(name ="message") val message: String, @Json(name ="user") val user: UserJson)

@JsonClass(generateAdapter=true)
data class LoginAuthToken(@Json(name ="token") val token: String)