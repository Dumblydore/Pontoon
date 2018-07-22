package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName

data class User(@SerializedName("id") val id: String,
                @SerializedName("profileImage") val profileImage: Image,
                @SerializedName("username") val username: String) {
    data class Container(@SerializedName("id") val id: String, @SerializedName("user") val user: User)
    data class Response(@SerializedName("id") val id: String, @SerializedName("users") val users: List<User.Container>)
}