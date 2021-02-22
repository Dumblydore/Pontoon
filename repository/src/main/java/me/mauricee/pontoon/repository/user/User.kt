package me.mauricee.pontoon.repository.user

import com.dropbox.android.external.store4.SourceOfTruth
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.data.local.user.UserActivityJoin
import me.mauricee.pontoon.data.local.user.UserDao
import me.mauricee.pontoon.data.local.user.UserEntity
import me.mauricee.pontoon.data.local.user.activity.ActivityDao
import me.mauricee.pontoon.data.local.user.activity.ActivityEntity
import me.mauricee.pontoon.data.network.user.ActivityJson
import me.mauricee.pontoon.data.network.user.UserJson
import me.mauricee.pontoon.repository.user.activity.UserActivity
import me.mauricee.pontoon.repository.user.activity.toEntity
import me.mauricee.pontoon.repository.user.activity.toModel
import javax.inject.Inject

data class User(override val id: String, val username: String, val profileImage: String,
                val activity: List<UserActivity>) : Diffable<String>


class UserSourceOfTruth @Inject constructor(private val userDao: UserDao,
                                            private val activityDao: ActivityDao) : SourceOfTruth<String, UserSourceOfTruth.Raw, User> {
    data class Raw(val user: UserJson, val activity: List<ActivityJson>)

    override suspend fun delete(key: String) = userDao.removeUser(key).await()

    override suspend fun deleteAll() = userDao.removeAllUsers().await()

    override fun reader(key: String): Flow<User> {
        return userDao.getUser(key).map(UserActivityJoin::toModel).asFlow()
    }

    override suspend fun write(key: String, value: Raw) = Completable.fromAction {
        userDao.upsert(value.user.toEntity())
        activityDao.upsert(value.activity.map { it.toEntity(key) })
    }.await()
}

fun UserJson.toEntity() = UserEntity(id, username, profileImage.path)
fun UserActivityJoin.toModel() = User(entity.id, entity.username, entity.profileImage, activities.map(ActivityEntity::toModel))
fun UserEntity.toModel() = User(id, username, profileImage, emptyList())