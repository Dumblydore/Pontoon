package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreatorListItem(
        @SerializedName("about") val about: String,
        @SerializedName("cover") val cover: String,
        @SerializedName("description") val description: String,
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("urlname") val urlname: String,
        @SerializedName("owner") val owner: Owner,
        @SerializedName("subscriptions") val subscriptions: List<Any>) {


    @Keep
    data class Owner(@SerializedName("id") val id: String, @SerializedName("username") val name: String)
}