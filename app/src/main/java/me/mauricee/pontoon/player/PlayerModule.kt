package me.mauricee.pontoon.player

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.player.player.PlayerContract
import me.mauricee.pontoon.player.player.PlayerFragment

@Module
abstract class PlayerModule {

    @Binds
    abstract fun bindActivity(playerActivity: PlayerActivity): Activity

    @Binds
    abstract fun bindFullscreenCallback(playerActivity: PlayerActivity): PlayerContract.Controls

    @Binds
    abstract fun bindGestureEvents(playerActivity: PlayerActivity): GestureEvents

    @Binds
    abstract fun bindPage(playerActivity: PlayerActivity): EventTracker.Page

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment
}