package me.mauricee.pontoon.model.subscription

import com.nytimes.android.external.store3.base.impl.StalePolicy
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.model.creator.Creator
import javax.inject.Named

@Module
@InstallIn(ActivityRetainedComponent::class)
class SubscriptionModelModule {

    @Provides
    fun PontoonDatabase.providesSubscriptionDao(): SubscriptionDao = subscriptionDao

    @Provides
    @Named("Subscriptions")
    fun providesSubscriptionStoreRoom(api: FloatPlaneApi, subscriptionPersistor: SubscriptionPersistor): StoreRoom<List<Creator>, Unit> = StoreRoom.from({
        api.subscriptions.flatMap { subscriptions ->
            val creatorIds = subscriptions.map { it.creatorId }.distinct().toTypedArray()
            api.getCreators(*creatorIds).flatMap { creators ->
                val creatorMap = creators.map { it.id to it }.toMap()
                val userIds = creators.map { it.owner }.distinct().toTypedArray()
                api.getUsers(*userIds).map { users ->
                    val userMap = users.users.map { it.id to it }.toMap()
                    subscriptions.map {
                        val creator = creatorMap[it.creatorId] ?: error("")
                        val user = userMap[creator.owner]?.user ?: error("")
                        SubscriptionPersistor.Raw(it, creator, user)
                    }
                }
            }
        }
    }, subscriptionPersistor, StalePolicy.NETWORK_BEFORE_STALE)
}