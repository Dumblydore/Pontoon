package me.mauricee.pontoon.main

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.DetailsFragment
import me.mauricee.pontoon.main.details.comment.RepliesDialogFragment
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.main.player.PlayerFragment
import me.mauricee.pontoon.main.search.SearchFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment
import okhttp3.OkHttpClient

@Module
abstract class MainModule {

    @Binds
    abstract fun bindMainNavigator(mainActivity: MainActivity): MainContract.Navigator

    @Binds
    abstract fun bindGestureEvents(mainActivity: MainActivity): GestureEvents

    @Binds
    abstract fun bindPage(mainActivity: MainActivity): EventTracker.Page

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
    abstract fun contributeDetailsFragment(): DetailsFragment

    @ContributesAndroidInjector
    abstract fun contributeRepliesFragment(): RepliesDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoPlayerFragment(): PlayerFragment

    @Module
    companion object {

        @MainScope
        @Provides
        @JvmStatic
        fun player(okHttpClient: OkHttpClient,
                   session: MediaSessionCompat,
                   audioManager: AudioManager,
                   agent: String, sharedPreferences: SharedPreferences,
                   context: Context): Player =
                Player(ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector()),
                        HlsMediaSource.Factory(OkHttpDataSourceFactory(okHttpClient::newCall, agent, null)),
                        audioManager, sharedPreferences, session)
    }
}