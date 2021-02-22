package me.mauricee.pontoon.repository.subscription

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import me.mauricee.pontoon.data.local.PontoonDatabase
import me.mauricee.pontoon.data.local.subscription.SubscriptionDao
import me.mauricee.pontoon.repository.creator.Creator
import javax.inject.Named

@Module
@InstallIn(ActivityRetainedComponent::class)
class SubscriptionModelModule {

    @Provides
    fun PontoonDatabase.providesSubscriptionDao(): SubscriptionDao = subscriptionDao

    @Provides
    @Named("Subscriptions")
    fun providesSubscriptionStoreRoom(fetcher: SubscriptionFetcher, subscriptionTruth: SubscriptionSourceOfTruth): Store<Unit, List<Creator>> {
        return StoreBuilder.from(fetcher, subscriptionTruth).build()
    }
}