package me.mauricee.pontoon.main

import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.gms.cast.framework.CastContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.video.DetailsFragment
import me.mauricee.pontoon.main.details.video.DetailsModule
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.main.livestream.LiveStreamFragment
import me.mauricee.pontoon.main.livestream.LiveStreamModule
import me.mauricee.pontoon.main.player.PlayerContract
import me.mauricee.pontoon.main.player.PlayerFragment
import me.mauricee.pontoon.main.search.SearchFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment

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
    abstract fun contributeVideoDetailsFragment(): DetailsFragment

    @ContributesAndroidInjector(modules = [LiveStreamModule::class])
    abstract fun contributeLiveStreamFragment(): LiveStreamFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment

    @Module
    companion object {
        @MainScope
        @Provides
        @JvmStatic
        fun providesSharedPreferences(activity: MainActivity): MenuInflater = activity.menuInflater


        @Provides
        @MainScope
        @JvmStatic
        fun provideCastContext(context: MainActivity) = CastContext.getSharedInstance(context)

        @Provides
        @MainScope
        @JvmStatic
        fun providesCastExoPlayer(castContext: CastContext): CastPlayer = CastPlayer(castContext)
    }

}