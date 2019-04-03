package me.mauricee.pontoon.domain.floatplane

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class Subscription(@Json(name = "creator") val creatorId: String,
                        @Json(name = "startDate") val startDate: Instant,
                        @Json(name = "endDate") val endDate: Instant,
                        @Json(name = "plan") val plan: Plan)