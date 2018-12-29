package me.mauricee.pontoon.main

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.video.DetailsFragment
import me.mauricee.pontoon.main.details.video.DetailsModule
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.player.player.PlayerFragment
import me.mauricee.pontoon.main.search.SearchFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment
import me.mauricee.pontoon.player.player.PlayerContract

@Module
abstract class MainModule {

    @Binds
    abstract fun bindMainNavigator(mainActivity: MainActivity): MainContract.Navigator

    @Binds
    abstract fun bindFullscreenCallback(mainActivity: MainActivity): PlayerContract.Controls

    @Binds
    abstract fun bindGestureEvents(mainActivity: MainActivity): GestureEvents

    @Binds
    abstract fun bindPage(mainActivity: MainActivity): EventTracker.Page

    @Binds
    abstract fun bindLifecycleOwner(mainActivity: MainActivity): LifecycleOwner

    @Binds
    abstract fun bindActivity(mainActivity: MainActivity): Activity

    @ContributesAndroidInjector
    abstract fun contributeVideoFragment(): VideoFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFragment(): UserFragment

    @ContributesAndroidInjector
    abstract fun contributeCreatorFragment(): CreatorFragment

    @ContributesAndroidInjector
    abstract fun contributeCreatorListFragment(): CreatorListFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment

    @ContributesAndroidInjector(modules = [DetailsModule::class])
    abstract fun contributePlayerFragment(): DetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment

}