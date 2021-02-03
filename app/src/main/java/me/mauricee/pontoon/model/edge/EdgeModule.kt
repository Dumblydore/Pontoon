package me.mauricee.pontoon.model.edge

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import me.mauricee.pontoon.model.PontoonDatabase

@Module
@InstallIn(ActivityRetainedComponent::class)
object EdgeModule {
    @Provides
    fun providesEdgeDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.edgeDao
}