package me.mauricee.pontoon.domain.floatplane.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LoginRequest(@SerializedName("username") val username: String, @SerializedName("password") val password: String)

@Keep
data class LoginResponse(@SerializedName("message") val message: String, @SerializedName("user") val user: User)

@Keep
data class LoginAuthToken(@SerializedName("token") val token: String)