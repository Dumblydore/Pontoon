package me.mauricee.pontoon.repository

import android.content.Context
import androidx.datastore.createDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.mauricee.pontoon.data.local.PontoonDatabase
import me.mauricee.pontoon.data.local.session.SessionCredentialsSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModelModule {
    @Provides
    fun providesSessionCredentialsSerializer() = SessionCredentialsSerializer()

    @Provides
    @Singleton
    fun providesDatabase(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun Context.providesSessionCredentialsDataStore(serializerFactory: SessionCredentialsSerializer) = createDataStore("credentials", serializerFactory)
}