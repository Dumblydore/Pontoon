package me.mauricee.pontoon.data.network.creator.list

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import me.mauricee.pontoon.data.network.common.Image

@JsonClass(generateAdapter = true)
data class CreatorListItem(@Json(name = "about") val about: String,
                           @Json(name = "cover") val cover: Image,
                           @Json(name = "description") val description: String,
                           @Json(name = "id") val id: String,
                           @Json(name = "title") val title: String,
                           @Json(name = "urlname") val urlname: String,
                           @Json(name = "owner") val owner: Owner,
                           @Json(name = "subscriptionPlans") val subscriptions: List<Any>) {


    @JsonClass(generateAdapter = true)
    data class Owner(@Json(name = "id") val id: String, @Json(name = "username") val name: String)
}