package me.mauricee.pontoon.playback

import android.content.Context
import androidx.media2.session.MediaController
import androidx.media2.session.MediaSession
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.gms.cast.framework.CastContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.lindstrom.m3u8.parser.MasterPlaylistParser
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.playback.converters.HlsMediaItemConverter
import me.mauricee.pontoon.playback.providers.MediaItemProvider
import me.mauricee.pontoon.rx.Optional
import okhttp3.OkHttpClient
import java.util.concurrent.Executors

@Module
@InstallIn(ActivityRetainedComponent::class)
object PlaybackModule {

    @Provides
    @ActivityRetainedScoped
    fun providesPlaylistParser() = MasterPlaylistParser()

    @Provides
    @ActivityRetainedScoped
    fun providesAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

    @Provides
    @ActivityRetainedScoped
    fun providesHlsFactory(okHttpClient: OkHttpClient, authInterceptor: AuthInterceptor, agent: String): HlsMediaSource.Factory =
            HlsMediaSource.Factory(OkHttpDataSourceFactory(okHttpClient.newBuilder().addInterceptor(authInterceptor).build(), agent))


    @Provides
    @ActivityRetainedScoped
    fun SimpleExoPlayer.providesSessionPlayerConnector(converter: HlsMediaItemConverter): SessionPlayerConnector = SessionPlayerConnector(this, converter)

    @Provides
    @ActivityRetainedScoped
    fun Context.providesCastPlayer(): Optional<CastPlayer> = try {
        Optional.of(CastPlayer(CastContext.getSharedInstance(this)))
    } catch (e: Exception) {
        Optional.empty()
    }

    @Provides
    @ActivityRetainedScoped
    fun Context.providesLocalExoPlayer(audioAttributes: AudioAttributes, hlsFactory: HlsMediaSource.Factory): SimpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            .setAudioAttributes(audioAttributes, true)
            .setMediaSourceFactory(hlsFactory)
            .build()

    @Provides
    @ActivityRetainedScoped
    fun Context.providesMediaSessionSessionCallback(sessionPlayerConnector: SessionPlayerConnector,
                                                    mediaItemProvider: MediaItemProvider): MediaSession.SessionCallback {
        return SessionCallbackBuilder(this, sessionPlayerConnector)
                .setFastForwardIncrementMs(500)
                .setRewindIncrementMs(500)
                .setMediaItemProvider(mediaItemProvider)
                .build()
    }

    @Provides
    @ActivityRetainedScoped
    fun Context.providesMediaSession(player: SessionPlayerConnector, sessionCallback: MediaSession.SessionCallback): MediaSession {
        return MediaSession.Builder(this, player)
                .setId("PontoonPlayer")
                .setSessionCallback(Executors.newSingleThreadExecutor(), sessionCallback)
                .build()
    }

    @Provides
    @ActivityRetainedScoped
    fun Context.providesMediaController(session: MediaSession): MediaController = MediaController.Builder(this)
            .setSessionToken(session.token)
            .build()

}