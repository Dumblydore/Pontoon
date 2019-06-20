package me.mauricee.pontoon.model

import android.content.Context
import androidx.room.Room
import com.nytimes.android.external.store3.base.Fetcher
import com.nytimes.android.external.store3.base.impl.RealStoreBuilder
import com.nytimes.android.external.store3.base.impl.Store
import com.nytimes.android.external.store3.base.impl.StoreBuilder
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import com.nytimes.android.external.store3.base.room.RoomPersister
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.Creator
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.Subscription
import me.mauricee.pontoon.ext.loge
import me.mauricee.pontoon.model.comment.CommentDao
import me.mauricee.pontoon.model.comment.CommentEntity
import me.mauricee.pontoon.model.edge.EdgeDao
import me.mauricee.pontoon.model.edge.EdgeEntity
import me.mauricee.pontoon.model.subscription.SubscriptionDao
import me.mauricee.pontoon.model.subscription.SubscriptionEntity
import me.mauricee.pontoon.model.subscription.toEntity
import me.mauricee.pontoon.model.user.*

@Module
class ModelModule {

    @AppScope
    @Provides
    fun providesDatabase(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb").build()

    @AppScope
    @Provides
    fun providesUserDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.userDao

    @AppScope
    @Provides
    fun providesCreatorDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.creatorDao

    @AppScope
    @Provides
    fun providesVideoDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.videoDao

    @AppScope
    @Provides
    fun providesCommentDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.commentDao

    @AppScope
    @Provides
    fun providesSubscriptionDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.subscriptionDao

    @AppScope
    @Provides
    fun providesEdgeDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.edgeDao

    @AppScope
    @Provides
    fun providesCreatorStore(api: FloatPlaneApi, daoPersistor: CreatorDao.Persistor): StoreRoom<CreatorEntity, String> = StoreRoom.from({
        api.getCreator(it).map { it.first().toEntity() }.firstOrError()
    }, daoPersistor)

    @AppScope
    @Provides
    fun providesUserStore(api: FloatPlaneApi, daoPersistor: UserDao.Persistor): StoreRoom<UserEntity, String> = StoreRoom.from({
        api.getUsers(it).map { it.users.first().user!!.toEntity() }.singleOrError()
    }, daoPersistor)


    @AppScope
    @Provides
    fun providesEdgeStore(api: FloatPlaneApi, daoPersistor: EdgeDao.Persistor): StoreRoom<List<String>, EdgeDao.Persistor.EdgeType> = StoreRoom.from(
            { _ -> api.edges.flatMapIterable { it.edges }.map { EdgeEntity(it.allowStreaming, it.allowDownload, it.hostname) }.toList() }, daoPersistor)

    @AppScope
    @Provides
    fun providesSubscriptionFetcher(api: FloatPlaneApi): Fetcher<List<String>, Unit> = Fetcher { api.subscriptions.flatMapIterable { it }.map { it.creatorId }.toList() }

}