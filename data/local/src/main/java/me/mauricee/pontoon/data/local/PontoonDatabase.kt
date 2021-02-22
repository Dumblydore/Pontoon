package me.mauricee.pontoon.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import me.mauricee.pontoon.data.local.comment.CommentDao
import me.mauricee.pontoon.data.local.comment.CommentEntity
import me.mauricee.pontoon.data.local.comment.CommentInteractionType
import me.mauricee.pontoon.data.local.creator.CreatorDao
import me.mauricee.pontoon.data.local.creator.CreatorEntity
import me.mauricee.pontoon.data.local.subscription.SubscriptionDao
import me.mauricee.pontoon.data.local.subscription.SubscriptionEntity
import me.mauricee.pontoon.data.local.user.UserDao
import me.mauricee.pontoon.data.local.user.UserEntity
import me.mauricee.pontoon.data.local.user.activity.ActivityDao
import me.mauricee.pontoon.data.local.user.activity.ActivityEntity
import me.mauricee.pontoon.data.local.video.RelatedVideo
import me.mauricee.pontoon.data.local.video.RelatedVideoDao
import me.mauricee.pontoon.data.local.video.VideoDao
import me.mauricee.pontoon.data.local.video.VideoEntity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

@TypeConverters(value = [InstantTypeConverter::class])
@Database(entities = [UserEntity::class, ActivityEntity::class, CreatorEntity::class, VideoEntity::class, CommentEntity::class, SubscriptionEntity::class, RelatedVideo::class], version = 4, exportSchema = false)
abstract class PontoonDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val activityDao: ActivityDao
    abstract val videoDao: VideoDao
    abstract val relatedVideoDao: RelatedVideoDao
    abstract val creatorDao: CreatorDao
    abstract val commentDao: CommentDao
    abstract val subscriptionDao: SubscriptionDao
}

class InstantTypeConverter {

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun toLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? = if (value == null) LocalDateTime.now() else
        LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC)

    @TypeConverter
    fun toLong(value: LocalDateTime?): Long = (value
            ?: LocalDateTime.now()).toEpochSecond(ZoneOffset.UTC)

    @TypeConverter
    fun toCommentInteractionType(value: String?): CommentInteractionType? = value?.let(CommentInteractionType::valueOf)

    @TypeConverter
    fun toString(value: CommentInteractionType?): String? = value?.toString()
}