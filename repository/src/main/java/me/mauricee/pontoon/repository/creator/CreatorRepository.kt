package me.mauricee.pontoon.repository.creator

import com.dropbox.android.external.store4.Store
import io.reactivex.Flowable
import me.mauricee.pontoon.data.local.creator.CreatorEntity
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import me.mauricee.pontoon.data.network.creator.info.CreatorJson
import me.mauricee.pontoon.data.network.creator.list.CreatorListItem
import me.mauricee.pontoon.repository.DataModel
import me.mauricee.pontoon.repository.util.store.getAndFetch
import me.mauricee.pontoon.repository.util.store.getAsDataModel
import javax.inject.Inject

class CreatorRepository @Inject constructor(private val allCreatorStore: Store<Unit, List<Creator>>,
                                            private val creatorStore: Store<String, Creator>) {
    val allCreators: DataModel<List<Creator>>
        get() = allCreatorStore.getAsDataModel(Unit)

    fun getCreator(id: String): Flowable<Creator> = creatorStore.getAndFetch(id)
}

internal fun CreatorJson.toEntity(): CreatorEntity = CreatorEntity(id, title, urlname, about, description, cover?.path, owner)
internal fun CreatorListItem.toEntity(): CreatorEntity = CreatorEntity(id, title, urlname, about, description, cover?.path, owner.id)