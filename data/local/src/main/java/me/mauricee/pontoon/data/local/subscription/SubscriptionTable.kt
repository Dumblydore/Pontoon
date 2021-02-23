package me.mauricee.pontoon.data.local.subscription

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.creator.CreatorEntity
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import org.threeten.bp.Instant

@Entity(tableName = "Subscriptions", foreignKeys = [ForeignKey(entity = CreatorEntity::class, parentColumns = ["id"], childColumns = ["creator"], onDelete = ForeignKey.CASCADE)])
data class SubscriptionEntity(@PrimaryKey val creator: String,
                              val planId: String,
                              val startDate: Instant,
                              val endDate: Instant)

@Dao
abstract class SubscriptionDao : BaseDao<SubscriptionEntity>() {

    @Query("SELECT * FROM Creator INNER JOIN Subscriptions ON Subscriptions.creator=Creator.id")
    abstract fun getSubscriptions(): Observable<List<CreatorUserJoin>>

    @Query("DELETE FROM Subscriptions")
    abstract fun removeSubscriptions(): Completable

}