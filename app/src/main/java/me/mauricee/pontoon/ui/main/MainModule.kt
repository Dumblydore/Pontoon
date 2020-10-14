package me.mauricee.pontoon.ui.main

import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ui.main.creator.CreatorFragment
import me.mauricee.pontoon.ui.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.ui.main.details.DetailsFragment
import me.mauricee.pontoon.ui.main.details.DetailsModule
import me.mauricee.pontoon.ui.main.history.HistoryFragment
import me.mauricee.pontoon.ui.main.player.PlayerContract
import me.mauricee.pontoon.ui.main.player.PlayerFragment
import me.mauricee.pontoon.ui.main.search.SearchFragment
import me.mauricee.pontoon.ui.main.user.UserFragment
import me.mauricee.pontoon.ui.main.videos.VideoFragment

@Module
abstract class MainModule {

    @Binds
    abstract fun bindMainNavigator(mainActivity: MainActivity): MainContract.Navigator

    @Binds
    abstract fun bindFullscreenCallback(mainActivity: MainActivity): PlayerContract.Controls

    @Binds
    abstract fun bindPage(mainActivity: MainActivity): EventTracker.Page

    @Binds
    abstract fun bindLifecycleOwner(mainActivity: MainActivity): LifecycleOwner

    @Binds
    abstract fun bindActivity(mainActivity: MainActivity): AppCompatActivity

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

    @Module
    companion object {
        @MainScope
        @Provides
        @JvmStatic
        fun providesSharedPreferences(activity: MainActivity): MenuInflater = activity.menuInflater
    }

}