package me.mauricee.pontoon.model.comment

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import me.mauricee.pontoon.model.PontoonDatabase

@Module
@InstallIn(ActivityRetainedComponent::class)
object CommentModule {
    @Provides
    fun providesCommentDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.commentDao
}