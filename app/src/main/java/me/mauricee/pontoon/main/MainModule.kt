package me.mauricee.pontoon.main

import android.content.Context
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.isupatches.wisefy.WiseFy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.ExoPlayerAnalyticsListener
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.DetailsFragment
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.main.player.PlayerFragment
import me.mauricee.pontoon.main.search.SearchFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment
import me.mauricee.pontoon.model.preferences.Preferences
import okhttp3.OkHttpClient

@Module
abstract class MainModule {

    @Binds
    abstract fun bindMainNavigator(mainActivity: MainActivity): MainContract.Navigator

    @Binds
    abstract fun bindGestureEvents(mainActivity: MainActivity): GestureEvents

    @Binds
    abstract fun bindPage(mainActivity: MainActivity): EventTracker.Page

    @Binds
    abstract fun bindLifecycleOwner(mainActivity: MainActivity): LifecycleOwner

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

    @ContributesAndroidInjector
    abstract fun contributePlayerFragment(): DetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment

    @Module
    companion object {

        @MainScope
        @Provides
        @JvmStatic
        fun WiseFy(context: Context): WiseFy = WiseFy.Brains(context).getSmarts()

        @MainScope
        @Provides
        @JvmStatic
        fun exoPlayer(context: Context, listener: ExoPlayerAnalyticsListener) =
                ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector()).also {
                    it.addAnalyticsListener(listener)
                }

        @MainScope
        @Provides
        @JvmStatic
        fun HlsFactory(okHttpClient: OkHttpClient, agent: String) =
                HlsMediaSource.Factory(OkHttpDataSourceFactory(okHttpClient::newCall, agent, null))
    }
}