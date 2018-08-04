package me.mauricee.pontoon.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import me.mauricee.pontoon.model.comment.CommentDao
import me.mauricee.pontoon.model.comment.CommentEntity
import me.mauricee.pontoon.model.user.CreatorDao
import me.mauricee.pontoon.model.user.CreatorEntity
import me.mauricee.pontoon.model.user.UserDao
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.video.HistoryDao
import me.mauricee.pontoon.model.video.HistoryEntity
import me.mauricee.pontoon.model.video.VideoDao
import me.mauricee.pontoon.model.video.VideoEntity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset


@TypeConverters(value = [InstantTypeConverter::class])
@Database(entities = [UserEntity::class, CreatorEntity::class, VideoEntity::class,
    HistoryEntity::class, CommentEntity::class], version = 1)
abstract class PontoonDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val creatorDao: CreatorDao
    abstract val videoDao: VideoDao
    abstract val historyDao: HistoryDao
    abstract val commentDao: CommentDao
}

class InstantTypeConverter {

    @TypeConverter
    fun toInstant(value: Long?): Instant? = if (value == null) Instant.now() else Instant.ofEpochMilli(value)

    @TypeConverter
    fun toLong(value: Instant?): Long = (value ?: Instant.now()).toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? = if (value == null) LocalDateTime.now() else
        LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC)

    @TypeConverter
    fun toLong(value: LocalDateTime?): Long = (value
            ?: LocalDateTime.now()).toEpochSecond(ZoneOffset.UTC)
}