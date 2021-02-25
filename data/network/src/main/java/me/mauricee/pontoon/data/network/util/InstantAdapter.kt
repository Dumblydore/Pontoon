package me.mauricee.pontoon.data.network.util

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.Instant

class InstantAdapter  {
    @ToJson
    fun toJson(instant: Instant) = instant.toString()

    @FromJson
    fun fromJson(instant: String) = Instant.parse(instant)
}