package me.mauricee.pontoon.domain.floatplane

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

@Keep
data class SubscriptionJson(@SerializedName("creator") val creatorId: String,
                            @SerializedName("startDate") val startDate: Instant,
                            @SerializedName("endDate") val endDate: Instant,
                            @SerializedName("plan") val plan: Plan)