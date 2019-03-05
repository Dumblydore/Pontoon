package me.mauricee.pontoon.common.playback

import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.gms.cast.framework.CastContext
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
object PlayerModule {


    @Provides
    @Reusable
    @JvmStatic
    fun provideCastContext(appCompatActivity: AppCompatActivity) = CastContext.getSharedInstance(appCompatActivity)

    @Provides
    @Reusable
    @JvmStatic
    fun providesCastExoPlayer(castContext: CastContext): CastPlayer = CastPlayer(castContext)
}