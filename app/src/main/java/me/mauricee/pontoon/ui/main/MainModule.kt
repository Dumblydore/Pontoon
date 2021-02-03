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
import me.mauricee.pontoon.ui.main.creator.CreatorModule
import me.mauricee.pontoon.ui.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.ui.main.history.HistoryFragment
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.main.player.details.DetailsFragment
import me.mauricee.pontoon.ui.main.player.details.DetailsModule
import me.mauricee.pontoon.ui.main.player.playback.PlayerContract
import me.mauricee.pontoon.ui.main.player.playback.PlayerFragment
import me.mauricee.pontoon.ui.main.player.playback.PlayerModule
import me.mauricee.pontoon.ui.main.search.SearchFragment
import me.mauricee.pontoon.ui.main.user.UserFragment
import me.mauricee.pontoon.ui.main.user.UserModule
import me.mauricee.pontoon.ui.main.videos.VideoFragment

@Module
interface MainModule {
//
//    @Binds
//    fun bindMainNavigator(mainActivity: MainActivity): MainContract.Navigator
//
//    @Binds
//    fun bindFullscreenCallback(mainActivity: MainActivity): PlayerContract.Controls
//
//    @Binds
//    fun bindPage(mainActivity: MainActivity): EventTracker.Page
//
//    @Binds
//    fun bindLifecycleOwner(mainActivity: MainActivity): LifecycleOwner
//
//    @Binds
//    fun bindActivity(mainActivity: MainActivity): AppCompatActivity
//
//
//    @ContributesAndroidInjector
//    fun contributeVideoFragment(): VideoFragment
//
//    @ContributesAndroidInjector
//    fun contributeSearchFragment(): SearchFragment
//
//    @ContributesAndroidInjector(modules = [UserModule::class])
//    fun contributeUserFragment(): UserFragment
//
//    @ContributesAndroidInjector(modules = [CreatorModule::class])
//    fun contributeCreatorFragment(): CreatorFragment
//
//    @ContributesAndroidInjector
//    fun contributeCreatorListFragment(): CreatorListFragment
//
//    @ContributesAndroidInjector
//    fun contributeHistoryFragment(): HistoryFragment
//
//    @ContributesAndroidInjector(modules = [DetailsModule::class])
//    fun contributePlayerFragment(): DetailsFragment
//
//    @ContributesAndroidInjector(modules = [PlayerModule::class])
//    fun contributeVideoPlayerFragment(): PlayerFragment
//
//    companion object {
//        @Provides
//        @MainScope
//        fun MainActivity.providesMenuInflater(): MenuInflater = menuInflater
//
//        @Provides
//        @MainScope
//        fun MainActivity.providesPlayerViewModel(): PlayerViewModel = playerViewModel
//    }

}