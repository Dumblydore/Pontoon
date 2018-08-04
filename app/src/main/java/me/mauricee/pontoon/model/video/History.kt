package me.mauricee.pontoon.model.video

import androidx.room.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

@Entity(tableName = "History", foreignKeys = [ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = ForeignKey.CASCADE)])
data class HistoryEntity(@PrimaryKey val videoId: String, val watched: LocalDateTime = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.id)))

class VideoHistory {
    @Embedded
    lateinit var history: HistoryEntity
    @Embedded
    lateinit var video: VideoEntity
}

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg history: HistoryEntity)
}