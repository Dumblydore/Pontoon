package me.mauricee.pontoon.main

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.main.details.DetailsFragment
import me.mauricee.pontoon.main.player.PlayerFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment

@Module
abstract class MainModule {

    @Binds
    abstract fun bindMainNavigator(mainActivity: MainActivity): MainContract.Navigator

    @Binds
    abstract fun bindGestureEvents(mainActivity: MainActivity): GestureEvents

    @ContributesAndroidInjector
    abstract fun contributeVideoFragment(): VideoFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFragment(): UserFragment

    @ContributesAndroidInjector
    abstract fun contributeCreatorFragment(): CreatorFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment

    @ContributesAndroidInjector
    abstract fun contributePlayerFragment(): DetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment

}