package me.mauricee.pontoon.data.network.user.subscription

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter=true)
data class Plan(@Json(name ="currency") val currency: String,
                @Json(name ="description") val description: String,
                @Json(name ="id") val id: String,
                @Json(name ="interval") val interval: String,
                @Json(name ="intervalCount") val intervalCount: Int,
                @Json(name ="owner") val owner: String,
                @Json(name ="price") val price: String,
                @Json(name ="title") val title: String)