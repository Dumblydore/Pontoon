package me.mauricee.pontoon.model.creator

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.ext.getAsDataModel
import me.mauricee.pontoon.model.DataModel
import me.mauricee.pontoon.ui.main.MainScope
import javax.inject.Inject

@MainScope
class CreatorRepository @Inject constructor(private val allCreatorStore: StoreRoom<List<Creator>, Unit>, private val creatorStore: StoreRoom<Creator, String>) {
    val allCreators: DataModel<List<Creator>>
        get() = allCreatorStore.getAsDataModel(Unit)

    fun getCreator(id: String): Observable<Creator> = creatorStore.getAndFetch(id)
}