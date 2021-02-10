package me.mauricee.pontoon.model.user

import com.nytimes.android.external.store3.base.impl.StalePolicy
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.rx.RxTuple

@Module
@InstallIn(ActivityRetainedComponent::class)
object UserModelModule {
    @Provides
    fun PontoonDatabase.providesUserDao() = userDao

    @Provides
    fun PontoonDatabase.providesActivityDao() = activityDao

    @Provides
    fun providesUserStoreRoom(api: FloatPlaneApi, userPersistor: UserPersistor): StoreRoom<User, String> = StoreRoom.from({ key ->
        RxTuple.zipAsPair(api.getUsers(key), api.getActivity(key)).map { pair ->
            val (user, activity) = pair
            UserPersistor.Raw(user.users.first().user!!, activity.activity)
        }
    }, userPersistor, StalePolicy.NETWORK_BEFORE_STALE)
}