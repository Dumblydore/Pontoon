package me.mauricee.pontoon.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import me.mauricee.pontoon.model.comment.CommentDao
import me.mauricee.pontoon.model.comment.CommentEntity
import me.mauricee.pontoon.model.edge.EdgeDao
import me.mauricee.pontoon.model.edge.EdgeEntity
import me.mauricee.pontoon.model.subscription.SubscriptionDao
import me.mauricee.pontoon.model.subscription.SubscriptionEntity
import me.mauricee.pontoon.model.creator.CreatorDao
import me.mauricee.pontoon.model.creator.CreatorEntity
import me.mauricee.pontoon.model.user.UserDao
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.user.activity.ActivityDao
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import me.mauricee.pontoon.model.video.VideoDao
import me.mauricee.pontoon.model.video.VideoEntity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

@TypeConverters(value = [InstantTypeConverter::class])
@Database(entities = [UserEntity::class, ActivityEntity::class, EdgeEntity::class, CreatorEntity::class, VideoEntity::class, CommentEntity::class, SubscriptionEntity::class], version = 2, exportSchema = false)
abstract class PontoonDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val activityDao: ActivityDao

    abstract val edgeDao: EdgeDao
    abstract val videoDao: VideoDao
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
}