package me.mauricee.pontoon.data.network.user.subscription

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json
import org.threeten.bp.Instant

@JsonClass(generateAdapter=true)
data class SubscriptionJson(@Json(name ="creator") val creatorId: String,
                            @Json(name ="startDate") val startDate: Instant,
                            @Json(name ="endDate") val endDate: Instant,
                            @Json(name ="plan") val plan: Plan)