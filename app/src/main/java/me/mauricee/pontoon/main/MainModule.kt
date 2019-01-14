package me.mauricee.pontoon.main

import android.app.Activity
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.gms.cast.framework.CastContext
import com.isupatches.wisefy.WiseFy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.DetailsFragment
import me.mauricee.pontoon.main.details.DetailsModule
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

    @Module
    companion object {

        @Provides
        @MainScope
        @JvmStatic
        fun providesSession(context: Context): MediaSessionCompat =
                MediaSessionCompat(context, "Pontoon")

        @Provides
        @MainScope
        @JvmStatic
        fun providesWiseFy(context: Context): WiseFy = WiseFy.Brains(context).getSmarts()

        @Provides
        @MainScope
        @JvmStatic
        fun providesExoPlayer(context: Context) = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
                .apply {
                    videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                }

        @Provides
        @MainScope
        @JvmStatic
        fun providesCastContext(context: Context) = CastContext.getSharedInstance(context)
    }
}