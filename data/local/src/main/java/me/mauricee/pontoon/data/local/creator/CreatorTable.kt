package me.mauricee.pontoon.data.local.creator

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.user.UserEntity

@Entity(tableName = "Creator", indices = [Index("owner")], foreignKeys = [ForeignKey(parentColumns = ["id"], childColumns = ["owner"], entity = UserEntity::class, onDelete = ForeignKey.CASCADE)])
class CreatorEntity(@PrimaryKey
                    val id: String,
                    val name: String,
                    val urlName: String,
                    val about: String,
                    val description: String,
                    val owner: String)

data class CreatorUserJoin(@Embedded val entity: CreatorEntity,
                           @Relation(parentColumn = "owner", entityColumn = "id") val user: UserEntity)

@Dao
abstract class CreatorDao : BaseDao<CreatorEntity>() {
    @Query("SELECT * FROM Creator")
    abstract fun getCreators(): Flowable<List<CreatorUserJoin>>

    @Query("SELECT * FROM Creator WHERE id=:creatorId")
    abstract fun getCreator(creatorId: String): Flowable<CreatorUserJoin>

    @Query("DELETE FROM Creator WHERE id=:creatorId")
    abstract fun removeCreator(creatorId: String): Completable

    @Query("DELETE FROM Creator")
    abstract fun removeAllCreators(): Completable
}
