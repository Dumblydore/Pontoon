package me.mauricee.pontoon.model.creator

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.ui.main.MainScope
import javax.inject.Inject

@MainScope
class CreatorRepository @Inject constructor(private val allCreatorStore: StoreRoom<List<Creator>, Unit>, private val creatorStore: StoreRoom<Creator, String>) {
    val allCreators: Observable<List<Creator>>
        get() = allCreatorStore.getAndFetch(Unit)

    fun getCreator(id: String): Observable<Creator> = creatorStore.getAndFetch(id)
}