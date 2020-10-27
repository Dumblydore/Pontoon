package me.mauricee.pontoon.ui.main.player.details

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.ui.main.player.details.comment.CommentDialogFragment
import me.mauricee.pontoon.ui.main.player.details.replies.RepliesDialogFragment

@Module
interface DetailsModule {
    @ContributesAndroidInjector
    fun contributeRepliesFragment(): RepliesDialogFragment

    @ContributesAndroidInjector
    fun contributeCommentFragment(): CommentDialogFragment

    @Binds
    fun bindNavigator(fragment: DetailsFragment) : DetailsContract.Navigator
}