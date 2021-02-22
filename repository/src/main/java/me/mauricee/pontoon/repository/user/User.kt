package me.mauricee.pontoon.repository.user

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.ActivityJson
import me.mauricee.pontoon.domain.floatplane.UserJson
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.Diffable
import me.mauricee.pontoon.data.local.user.activity.ActivityDao
import me.mauricee.pontoon.data.local.user.activity.ActivityEntity
import me.mauricee.pontoon.data.local.user.activity.toEntity
import javax.inject.Inject

@Entity(tableName = "User")
data class UserEntity(@PrimaryKey override val id: String, val username: String, val profileImage: String) : Diffable<String>

@Dao
abstract class UserDao : BaseDao<UserEntity>() {
    @Query("SELECT * FROM User WHERE id=:userId")
    abstract fun getUser(userId: String): Observable<User>
}

data class User(@Embedded val entity: UserEntity,
                @Relation(parentColumn = "id", entityColumn = "userId") val activities: List<ActivityEntity>) : Diffable<String> {
    @Ignore
    override val id: String = entity.id
}

fun UserJson.toEntity() = UserEntity(id, username, profileImage.path)

class UserPersistor @Inject constructor(private val userDao: UserDao,
                                        private val activityDao: ActivityDao) : RoomPersister<UserPersistor.Raw, User, String> {

    override fun read(key: String): Observable<User> = userDao.getUser(key)

    override fun write(key: String, raw: Raw) {
        userDao.upsert(raw.user.toEntity())
        activityDao.upsert(raw.activity.map { it.toEntity(key) })
    }

    data class Raw(val user: UserJson, val activity: List<ActivityJson>)
}