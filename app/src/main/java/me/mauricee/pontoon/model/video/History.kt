package me.mauricee.pontoon.model.video

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import io.reactivex.Flowable

@Entity(tableName = "History", foreignKeys = [ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = CASCADE)])
data class HistoryEntity(@PrimaryKey val videoId: String)

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg history: HistoryEntity)

    @Query("SELECT * FROM History")
    fun history(): Flowable<List<HistoryEntity>>
}