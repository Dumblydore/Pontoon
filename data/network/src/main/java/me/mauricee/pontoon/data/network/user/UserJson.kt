package me.mauricee.pontoon.data.network.user

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import me.mauricee.pontoon.data.network.common.Image

@JsonClass(generateAdapter=true)
data class UserJson(@Json(name ="id") val id: String,
                    @Json(name ="profileImage") val profileImage: Image,
                    @Json(name ="username") val username: String) {
    @JsonClass(generateAdapter=true)
    data class Container(@Json(name ="id") val id: String?,
                         @Json(name ="user") val user: UserJson?,
                         @Json(name ="needs2FA") val needs2Fa: Boolean?)

    @JsonClass(generateAdapter=true)
    data class Response(@Json(name = "id") val id: String?, @Json(name = "users") val users: List<Container>)
}