package me.mauricee.pontoon.model

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.model.creator.CreatorModelModule
import me.mauricee.pontoon.model.user.UserModelModule

@Module(includes = [UserModelModule::class, CreatorModelModule::class])
class ModelModule {

    @AppScope
    @Provides
    fun providesDatabase(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb")
            .fallbackToDestructiveMigration()
            .build()

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
}