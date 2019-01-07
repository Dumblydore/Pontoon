package me.mauricee.pontoon.model.livestream

import com.google.gson.annotations.SerializedName

data class RequestMessage<T>(@SerializedName("method")
                             val method: String,
                             @SerializedName("data")
                             val data: T)