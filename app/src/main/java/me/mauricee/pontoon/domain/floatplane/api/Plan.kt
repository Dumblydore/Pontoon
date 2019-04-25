package me.mauricee.pontoon.domain.floatplane.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Plan(@SerializedName("currency") val currency: String,
                @SerializedName("description") val description: String,
                @SerializedName("id") val id: String,
                @SerializedName("interval") val interval: String,
                @SerializedName("intervalCount") val intervalCount: Int,
                @SerializedName("owner") val owner: String,
                @SerializedName("price") val price: String,
                @SerializedName("title") val title: String)