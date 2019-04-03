package me.mauricee.pontoon.common.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.Instant

class Converters {
    @ToJson
    fun toJson(instant: Instant) = instant.toString()

    @FromJson
    fun fromJson(instant: String) = Instant.parse(instant)

}