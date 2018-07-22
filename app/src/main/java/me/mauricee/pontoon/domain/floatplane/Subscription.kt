package me.mauricee.pontoon.domain.floatplane

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

data class Subscription(
        @SerializedName("creator")val creatorId: String,
        @SerializedName("startDate")val startDate: Instant,
        @SerializedName("endDate")val endDate: Instant
)