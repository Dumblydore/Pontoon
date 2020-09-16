package me.mauricee.pontoon.model.creator

import androidx.room.*
import io.reactivex.Observable

@Entity(tableName = "Creator")
class CreatorEntity(@PrimaryKey val id: String, val name: String,
                    val urlName: String, val about: String,
                    val description: String, val owner: String)

@Dao
interface CreatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg creatorEntity: CreatorEntity)

    @Update
    fun update(vararg creatorEntity: CreatorEntity)

    @Delete
    fun delete(vararg creatorEntity: CreatorEntity)

    @Query("SELECT * FROM Creator WHERE id IN (:creatorIds)")
    fun getCreatorsByIds(vararg creatorIds: String): Observable<List<CreatorEntity>>

}