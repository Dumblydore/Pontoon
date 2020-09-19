package me.mauricee.pontoon.model.creator

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.throwIfEmpty
import me.mauricee.pontoon.model.PontoonDatabase

@Module
class CreatorModelModule {

    @Provides
    fun PontoonDatabase.providesCreatorDao() = creatorDao

    @Provides
    fun providesCreatorPersistor(api: FloatPlaneApi, persistor: CreatorPersistor): StoreRoom<Creator, String> = StoreRoom.from({ key ->
        api.getCreators(key).throwIfEmpty().map { it.first() }.flatMap { creator ->
            api.getUsers(creator.owner).map { it.users }.throwIfEmpty().map { it.first() }.map { owner ->
                CreatorPersistor.Raw(creator, owner.user!!)
            }
        }
    }, persistor)

    @Provides
    fun providesAllCreatorsPersistor(api: FloatPlaneApi, persistor: AllCreatorPersistor): StoreRoom<List<Creator>, Unit> = StoreRoom.from({ key ->
        api.allCreators.throwIfEmpty().flatMap { creators ->
            val ownerIds = creators.map { it.owner.id }.toTypedArray()
            api.getUsers(*ownerIds).map { it.users }.throwIfEmpty().map { ownerList ->
                val owners = ownerList.map { it.user!!.id to it.user }.toMap()
                creators.map {
                    AllCreatorPersistor.Raw(it, owners[it.id] ?: error("Couldn't find Creator"))
                }
            }
        }
    }, persistor)
}