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
    fun providesCreatorStore(api: FloatPlaneApi, daoPersistor: CreatorDao.Persistor): StoreRoom<List<CreatorEntity>, String> = StoreRoom.from({
        api.getCreator(it).map { it.first().toEntity() }.firstOrError()
    }, daoPersistor)

    @AppScope
    @Provides
    fun providesUserStore(api: FloatPlaneApi, daoPersistor: UserDao.Persistor): StoreRoom<List<UserEntity>, String> = StoreRoom.from({
        api.getUsers(it).map { it.users.first().user!!.toEntity() }.singleOrError()
    }, daoPersistor)

}