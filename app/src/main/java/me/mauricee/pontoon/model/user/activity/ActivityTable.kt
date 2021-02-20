package me.mauricee.pontoon.model.user.activity

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import me.mauricee.pontoon.domain.floatplane.ActivityJson
import me.mauricee.pontoon.model.BaseDao
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.model.user.UserEntity
import org.threeten.bp.Instant

@Entity(tableName = "Activities", foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)])
data class ActivityEntity(val userId: String,
                          val comment: String,
                          val date: Instant,
                          val postId: String?) : Diffable<Long> {

    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0L
}

@Dao
abstract class ActivityDao : BaseDao<ActivityEntity>()

fun ActivityJson.toEntity(userId: String) = ActivityEntity(userId, comment, date, postId)