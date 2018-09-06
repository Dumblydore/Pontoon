package me.mauricee.pontoon.model.subscription

import androidx.room.*
import io.reactivex.Observable
import org.threeten.bp.Instant

@Entity(tableName = "Subscription")
data class SubscriptionEntity(@PrimaryKey val creator: String, val planId: String, val startDate: Instant, val endDate: Instant)

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg subscriptionEntity: SubscriptionEntity)

    @Update
    fun update(vararg subscriptionEntity: SubscriptionEntity)

    @Query("SELECT * FROM Subscription")
    fun getSubscriptions(): Observable<List<SubscriptionEntity>>

    @Delete
    fun delete(vararg subscriptionEntity: SubscriptionEntity)
}