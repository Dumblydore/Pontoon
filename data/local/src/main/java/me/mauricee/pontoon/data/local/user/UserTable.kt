package me.mauricee.pontoon.data.local.user

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.user.activity.ActivityEntity

@Entity(tableName = "User")
data class UserEntity(@PrimaryKey val id: String, val username: String, val profileImage: String)

@Dao
abstract class UserDao : BaseDao<UserEntity>() {
    @Query("SELECT * FROM User WHERE id=:userId")
    abstract fun getUser(userId: String): Observable<UserActivityJoin>

    @Query("DELETE FROM User WHERE id=:userId")
    abstract fun removeUser(userId: String): Completable

    @Query("DELETE FROM User")
    abstract fun removeAllUsers(): Completable
}

data class UserActivityJoin(@Embedded val entity: UserEntity,
                            @Relation(parentColumn = "id", entityColumn = "userId") val activities: List<ActivityEntity>)
