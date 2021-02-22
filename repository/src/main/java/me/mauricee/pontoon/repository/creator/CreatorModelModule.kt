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
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import me.mauricee.pontoon.data.local.creator.CreatorDao
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.creator.info.CreatorJson
import me.mauricee.pontoon.data.network.creator.list.CreatorListItem

@Module
@InstallIn(ActivityRetainedComponent::class)
object CreatorModelModule {

    @Provides
    fun PontoonDatabase.providesCreatorDao() = creatorDao

    fun providesCreatorStore(floatPlaneApi: FloatPlaneApi,
                             creatorDao: CreatorDao): Store<String, CreatorUserJoin> {
        return StoreBuilder.from(Fetcher.ofSingle { key -> floatPlaneApi.getCreators(key).map(List<CreatorJson>::first).map(CreatorJson::toEntity) },
                SourceOfTruth.ofFlowable(
                        reader = creatorDao::getCreator,
                        writer = { _, creator -> Completable.fromAction { creatorDao.upsert(creator) } },
                        delete = creatorDao::removeCreator,
                        deleteAll = creatorDao::removeAllCreators
                )
        ).build()
    }

    fun providesAllCreatorStore(floatPlaneApi: FloatPlaneApi,
                                creatorDao: CreatorDao): Store<Unit, List<CreatorUserJoin>> {
        return StoreBuilder.from(Fetcher.ofSingle { _: Unit ->
            floatPlaneApi.allCreators
                    .flatMapObservable(List<CreatorListItem>::toObservable)
                    .map(CreatorListItem::toEntity)
                    .toList()
        }, SourceOfTruth.ofFlowable(
                reader = { creatorDao.getCreators() },
                writer = { _, creators -> Completable.fromAction { creatorDao.upsert(creators) } },
                delete = { creatorDao.removeAllCreators() },
        )
        ).build()
    }
}