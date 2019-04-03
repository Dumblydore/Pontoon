package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(@Json(name = "id") val id: String,
                @Json(name = "profileImage") val profileImage: Image,
                @Json(name = "username") val username: String) {
    @JsonClass(generateAdapter = true)
    data class Container(@Json(name = "id") val id: String?,
                         @Json(name = "user") val user: User?,
                         @Json(name = "needs2FA") val needs2Fa: Boolean?)

    @JsonClass(generateAdapter = true)
    data class Response(@Json(name = "id") val id: String, @Json(name = "users") val users: List<User.Container>)
}