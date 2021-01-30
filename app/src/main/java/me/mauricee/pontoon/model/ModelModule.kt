package me.mauricee.pontoon.model

import android.content.Context
import androidx.datastore.createDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.model.creator.CreatorModelModule
import me.mauricee.pontoon.model.session.SessionCredentialsSerializer
import me.mauricee.pontoon.model.session.SessionCredentialsSerializer_Factory
import me.mauricee.pontoon.model.subscription.SubscriptionModelModule
import me.mauricee.pontoon.model.user.UserModelModule
import me.mauricee.pontoon.model.video.VideoModelModule

@Module(includes = [UserModelModule::class, CreatorModelModule::class, SubscriptionModelModule::class, VideoModelModule::class])
object ModelModule {

    @AppScope
    @Provides
    fun providesDatabase(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb")
            .fallbackToDestructiveMigration()
            .build()

    @AppScope
    @Provides
    fun providesCommentDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.commentDao

    @AppScope
    @Provides
    fun providesEdgeDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.edgeDao

    @AppScope
    @Provides
    fun Context.providesSessionCredentialsDataStore(serializerFactory: SessionCredentialsSerializer) = createDataStore("credentials", serializerFactory)
}