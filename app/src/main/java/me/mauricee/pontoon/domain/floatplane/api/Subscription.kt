package me.mauricee.pontoon.domain.floatplane.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

@Keep
data class Subscription(@SerializedName("creator") val creatorId: String,
                        @SerializedName("startDate") val startDate: Instant,
                        @SerializedName("endDate") val endDate: Instant,
                        @SerializedName("plan") val plan: Plan)