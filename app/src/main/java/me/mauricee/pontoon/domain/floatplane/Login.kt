package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName

data class LoginRequest(@SerializedName("username") val username: String, @SerializedName("password") val password: String)
data class LoginResponse(@SerializedName("message") val message: String, @SerializedName("user") val user: User)