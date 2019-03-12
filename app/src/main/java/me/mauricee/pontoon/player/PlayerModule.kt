package me.mauricee.pontoon.player

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
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
    @PlayerScope
    abstract fun bindActivity(playerActivity: PlayerActivity): AppCompatActivity

    @Binds
    @PlayerScope
    abstract fun bindLifecycleOwner(mainActivity: PlayerActivity): LifecycleOwner

    @Binds
    @PlayerScope
    abstract fun bindFullscreenCallback(playerActivity: PlayerActivity): PlayerContract.Controls

    @Binds
    @PlayerScope
    abstract fun bindGestureEvents(playerActivity: PlayerActivity): GestureEvents

    @Binds
    @PlayerScope
    abstract fun bindPage(playerActivity: PlayerActivity): EventTracker.Page

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment
}