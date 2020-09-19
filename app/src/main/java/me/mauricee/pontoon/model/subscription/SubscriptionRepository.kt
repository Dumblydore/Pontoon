package me.mauricee.pontoon.model.subscription

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.main.MainScope
import me.mauricee.pontoon.model.creator.Creator
import javax.inject.Inject
import javax.inject.Named

@MainScope
class SubscriptionRepository @Inject constructor(@Named("Subscriptions") private val subscriptionStore: StoreRoom<List<Creator>, Unit>) {
    val subscriptions: Observable<List<Creator>>
        get() = subscriptionStore.getAndFetch(Unit)
}