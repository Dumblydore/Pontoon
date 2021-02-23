package me.mauricee.pontoon.repository.subscription

import com.dropbox.android.external.store4.Store
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import me.mauricee.pontoon.repository.DataModel
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.util.store.getAsDataModel
import javax.inject.Inject
import javax.inject.Named

class SubscriptionRepository @Inject constructor(@Named("Subscriptions") private val subscriptionStore: Store<Unit, List<Creator>>) {
    val subscriptions: DataModel<List<Creator>>
        get() = subscriptionStore.getAsDataModel(Unit)
}