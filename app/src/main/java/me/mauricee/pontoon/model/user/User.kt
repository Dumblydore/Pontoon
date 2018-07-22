package me.mauricee.pontoon.model.user

import androidx.room.*
import io.reactivex.Single

@Entity(tableName = "User")
data class UserEntity(@PrimaryKey val id: String, val username: String, val profileImage: String)
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg use: UserEntity)

    @Delete
    fun delete(vararg use: UserEntity)

    @Query("SELECT * FROM User WHERE id IN (:userIds)")
    fun getUsersByIds(vararg userIds: String) : Single<List<UserEntity>>

}