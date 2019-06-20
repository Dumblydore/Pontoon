package me.mauricee.pontoon.model.subscription

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.Subscription
import me.mauricee.pontoon.ext.logd
import org.threeten.bp.Instant
import javax.inject.Inject

@Entity(tableName = "Subscription")
data class SubscriptionEntity(@PrimaryKey val creator: String, val planId: String, val startDate: Instant, val endDate: Instant)

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg subscriptionEntity: SubscriptionEntity)

    @Query("SELECT creator FROM Subscription")
    fun getSubscriptions(): Observable<List<String>>

    @Delete
    fun delete(vararg subscriptionEntity: SubscriptionEntity)

    class Persistor @Inject constructor(private val dao: SubscriptionDao) : RoomPersister<List<SubscriptionEntity>, List<String>, String> {
        override fun write(key: String, raw: List<SubscriptionEntity>) = dao.insert(*raw.toTypedArray())

        override fun read(key: String): Observable<List<String>> = dao.getSubscriptions()
    }
}

fun Subscription.toEntity(): SubscriptionEntity = SubscriptionEntity(creatorId, plan.id, startDate, endDate)