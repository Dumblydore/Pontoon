package me.mauricee.pontoon.model.subscription

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import me.mauricee.pontoon.ext.getAsDataModel
import me.mauricee.pontoon.model.DataModel
import me.mauricee.pontoon.model.creator.Creator
import javax.inject.Inject
import javax.inject.Named

class SubscriptionRepository @Inject constructor(@Named("Subscriptions") private val subscriptionStore: StoreRoom<List<Creator>, Unit>) {
    val subscriptions: DataModel<List<Creator>>
        get() = subscriptionStore.getAsDataModel(Unit)
}