package me.mauricee.pontoon.repository.subscription

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.CreatorJson
import me.mauricee.pontoon.domain.floatplane.SubscriptionJson
import me.mauricee.pontoon.domain.floatplane.UserJson
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.creator.Creator
import me.mauricee.pontoon.data.local.creator.CreatorDao
import me.mauricee.pontoon.data.local.creator.CreatorEntity
import me.mauricee.pontoon.data.local.creator.toEntity
import me.mauricee.pontoon.data.local.user.UserDao
import me.mauricee.pontoon.data.local.user.UserEntity
import me.mauricee.pontoon.data.local.user.toEntity
import org.threeten.bp.Instant
import javax.inject.Inject

@Entity(tableName = "Subscriptions", foreignKeys = [ForeignKey(entity = CreatorEntity::class, parentColumns = ["id"], childColumns = ["creator"], onDelete = ForeignKey.CASCADE)])
data class SubscriptionEntity(@PrimaryKey val creator: String,
                              val planId: String,
                              val startDate: Instant,
                              val endDate: Instant)

fun SubscriptionJson.toEntity(): SubscriptionEntity = SubscriptionEntity(creatorId, plan.id, startDate, endDate)

@Dao
abstract class SubscriptionDao : BaseDao<SubscriptionEntity>() {

    @Query("SELECT * FROM Creator INNER JOIN Subscriptions ON Subscriptions.creator=Creator.id")
    abstract fun getSubscriptions(): Observable<List<Creator>>

}

class SubscriptionPersistor @Inject constructor(private val subscriptionDao: SubscriptionDao,
                                                private val creatorDao: CreatorDao,
                                                private val userDao: UserDao) : RoomPersister<List<SubscriptionPersistor.Raw>, List<Creator>, Unit> {

    data class Raw(val subscription: SubscriptionJson, val creator: CreatorJson, val user: UserJson)

    override fun read(key: Unit): Observable<List<Creator>> = subscriptionDao.getSubscriptions()

    override fun write(key: Unit, raw: List<Raw>) {
        val users = mutableListOf<UserEntity>()
        val creators = mutableListOf<CreatorEntity>()
        val subscriptions = mutableListOf<SubscriptionEntity>()
        raw.forEach {
            users.add(it.user.toEntity())
            creators.add(it.creator.toEntity())
            subscriptions.add(it.subscription.toEntity())
        }
        userDao.upsert(users)
        creatorDao.upsert(creators)
        subscriptionDao.upsert(subscriptions)
    }
}