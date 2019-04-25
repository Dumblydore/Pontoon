package me.mauricee.pontoon.domain.floatplane.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class User(@SerializedName("id") val id: String,
                @SerializedName("profileImage") val profileImage: Image,
                @SerializedName("username") val username: String) {
    @Keep
    data class Container(@SerializedName("id") val id: String?,
                         @SerializedName("user") val user: User?,
                         @SerializedName("needs2FA") val needs2Fa: Boolean)

    @Keep
    data class Response(@SerializedName("id") val id: String, @SerializedName("users") val users: List<Container>)
}