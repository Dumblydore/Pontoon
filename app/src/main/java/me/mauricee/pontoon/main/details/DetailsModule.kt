package me.mauricee.pontoon.main.details

import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.main.details.comment.CommentDialogFragment
import me.mauricee.pontoon.main.details.replies.RepliesDialogFragment

@Module
interface DetailsModule {
    @ContributesAndroidInjector
    fun contributeRepliesFragment(): RepliesDialogFragment

    @ContributesAndroidInjector
    fun contributeCommentFragment(): CommentDialogFragment
}