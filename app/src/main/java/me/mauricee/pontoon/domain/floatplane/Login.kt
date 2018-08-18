package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LoginRequest(@SerializedName("username") val username: String, @SerializedName("password") val password: String)
@Keep
data class LoginResponse(@SerializedName("message") val message: String, @SerializedName("user") val user: User)