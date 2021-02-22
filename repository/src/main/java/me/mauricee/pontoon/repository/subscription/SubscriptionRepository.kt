package me.mauricee.pontoon.repository.subscription

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import me.mauricee.pontoon.ext.getAsDataModel
import me.mauricee.pontoon.repository.DataModel
import me.mauricee.pontoon.data.local.creator.Creator
import javax.inject.Inject
import javax.inject.Named

class SubscriptionRepository @Inject constructor(@Named("Subscriptions") private val subscriptionStore: StoreRoom<List<Creator>, Unit>) {
    val subscriptions: DataModel<List<Creator>>
        get() = subscriptionStore.getAsDataModel(Unit)
}