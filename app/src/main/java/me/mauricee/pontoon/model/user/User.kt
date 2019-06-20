package me.mauricee.pontoon.model.user

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.User
import javax.inject.Inject

@Entity(tableName = "User")
data class UserEntity(@PrimaryKey val id: String, val username: String, val profileImage: String)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg use: UserEntity)

    @Delete
    fun delete(vararg use: UserEntity)

    @Query("SELECT * FROM User WHERE id IN (:userIds)")
    fun getUsersByIds(vararg userIds: String): Observable<List<UserEntity>>

    @Query("SELECT * FROM User WHERE id IS (:userId)")
    fun getUserById(userId: String): Observable<UserEntity>

    class Persistor @Inject constructor(private val dao: UserDao) : RoomPersister<UserEntity, UserEntity, String> {
        override fun write(key: String, raw: UserEntity) = dao.insert(raw)

        override fun read(key: String): Observable<UserEntity> = dao.getUserById(key)
    }
}

fun User.toEntity(): UserEntity = UserEntity(id, username, profileImage.path)