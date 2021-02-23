package me.mauricee.pontoon.repository.creator

import com.dropbox.android.external.store4.*
import com.dropbox.store.rx2.ofFlowable
import com.dropbox.store.rx2.ofSingle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.data.local.PontoonDatabase
import me.mauricee.pontoon.data.local.creator.CreatorDao
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.creator.info.CreatorJson
import me.mauricee.pontoon.data.network.creator.list.CreatorListItem

@Module
@InstallIn(ActivityRetainedComponent::class)
object CreatorModelModule {

    @Provides
    fun PontoonDatabase.providesCreatorDao() = creatorDao

    @Provides
    fun providesCreatorStore(floatPlaneApi: FloatPlaneApi,
                             creatorDao: CreatorDao): Store<String, Creator> {
        return StoreBuilder.from(Fetcher.ofSingle { key -> floatPlaneApi.getCreators(key).map(List<CreatorJson>::first).map(CreatorJson::toEntity) },
                SourceOfTruth.ofFlowable(
                        reader = { creatorDao.getCreator(it).map(CreatorUserJoin::toModel) },
                        writer = { _, creator -> Completable.fromAction { creatorDao.upsert(creator) } },
                        delete = creatorDao::removeCreator,
                        deleteAll = creatorDao::removeAllCreators
                )
        ).build()
    }

    @Provides
    fun providesAllCreatorStore(floatPlaneApi: FloatPlaneApi,
                                creatorDao: CreatorDao): Store<Unit, List<Creator>> {
        return StoreBuilder.from(Fetcher.ofSingle { _: Unit ->
            floatPlaneApi.allCreators
                    .flatMapObservable(List<CreatorListItem>::toObservable)
                    .map(CreatorListItem::toEntity)
                    .toList()
        }, SourceOfTruth.ofFlowable(
                reader = { creatorDao.getCreators().map { it.map(CreatorUserJoin::toModel) } },
                writer = { _, creators -> Completable.fromAction { creatorDao.upsert(creators) } },
                delete = { creatorDao.removeAllCreators() },
        )
        ).build()
    }
}