package me.mauricee.pontoon.repository.user

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.store.rx2.ofSingle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import io.reactivex.Single
import me.mauricee.pontoon.data.local.PontoonDatabase
import me.mauricee.pontoon.data.network.FloatPlaneApi

@Module
@InstallIn(ActivityRetainedComponent::class)
object UserModelModule {
    @Provides
    fun PontoonDatabase.providesUserDao() = userDao

    @Provides
    fun PontoonDatabase.providesActivityDao() = activityDao

    @Provides
    fun providesUserStoreRoom(api: FloatPlaneApi, userSourceOfTruth: UserSourceOfTruth): Store<String, User> {
        return StoreBuilder.from(
                Fetcher.ofSingle {
                    Single.zip(api.getUsers(it), api.getActivity(it), { user, activity ->
                        UserSourceOfTruth.Raw(user.users.first().user!!, activity.activity)
                    })
                }, userSourceOfTruth
        ).build()
    }
}