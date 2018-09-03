package me.mauricee.pontoon.model

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.di.AppScope

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
    fun providesHistoryDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.historyDao

    @AppScope
    @Provides
    fun providesCommentDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.commentDao

    @AppScope
    @Provides
    fun providesSubscriptionDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.subscriptionDao
}