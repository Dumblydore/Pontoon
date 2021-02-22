package me.mauricee.pontoon.repository.user.activity

import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.data.local.user.activity.ActivityEntity
import me.mauricee.pontoon.data.network.user.ActivityJson
import org.threeten.bp.Instant

fun ActivityJson.toEntity(userId: String) = ActivityEntity(userId, comment, date, postId)
fun ActivityEntity.toModel(): UserActivity = UserActivity(id, userId, postId, comment, date)

data class UserActivity(override val id: Long, val userId: String, val postId: String?, val comment: String, val date: Instant) : Diffable<Long>