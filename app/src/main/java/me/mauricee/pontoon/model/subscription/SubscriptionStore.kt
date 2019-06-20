package me.mauricee.pontoon.model.subscription

import com.nytimes.android.external.store3.base.Fetcher
import com.nytimes.android.external.store3.base.impl.RealStore
import io.reactivex.Single
import javax.inject.Inject

class SubscriptionStore @Inject constructor(fetcher: Fetcher<List<String>, Unit>) : RealStore<List<String>, Unit>(fetcher) {
    fun get(): Single<List<String>> = get(Unit)
}