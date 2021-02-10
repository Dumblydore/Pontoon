package me.mauricee.pontoon.model

import android.content.Context
import androidx.datastore.createDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.mauricee.pontoon.model.session.SessionCredentialsSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModelModule {
    @Provides
    @Singleton
    fun providesDatabase(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun Context.providesSessionCredentialsDataStore(serializerFactory: SessionCredentialsSerializer) = createDataStore("credentials", serializerFactory)
}