package me.mauricee.pontoon.playback

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
import io.lindstrom.m3u8.parser.MasterPlaylistParser
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.playback.converters.HlsMediaItemConverter
import me.mauricee.pontoon.playback.providers.MediaItemProvider
import me.mauricee.pontoon.rx.Optional
import me.mauricee.pontoon.ui.main.MainActivity
import me.mauricee.pontoon.ui.main.MainScope
import okhttp3.OkHttpClient
import java.util.concurrent.Executors

@Module
object PlaybackModule {

    @Provides
    @MainScope
    fun providesPlaylistParser() = MasterPlaylistParser()

    @Provides
    @MainScope
    fun providesAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build()

    @Provides
    @MainScope
    fun providesHlsFactory(okHttpClient: OkHttpClient, authInterceptor: AuthInterceptor, agent: String): HlsMediaSource.Factory =
            HlsMediaSource.Factory(OkHttpDataSourceFactory(okHttpClient.newBuilder().addInterceptor(authInterceptor).build(), agent))


    @Provides
    @MainScope
    fun SimpleExoPlayer.providesSessionPlayerConnector(converter: HlsMediaItemConverter): SessionPlayerConnector = SessionPlayerConnector(this, converter)

    @Provides
    @MainScope
    fun MainActivity.providesCastPlayer(): Optional<CastPlayer> = try {
        Optional.of(CastPlayer(CastContext.getSharedInstance(this)))
    } catch (e: Exception) {
        Optional.empty()
    }

    @Provides
    @MainScope
    fun MainActivity.providesLocalExoPlayer(audioAttributes: AudioAttributes, hlsFactory: HlsMediaSource.Factory): SimpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            .setAudioAttributes(audioAttributes, true)
            .setMediaSourceFactory(hlsFactory)
            .build()

    @Provides
    @MainScope
    fun MainActivity.providesMediaSessionSessionCallback(sessionPlayerConnector: SessionPlayerConnector,
                                                         mediaItemProvider: MediaItemProvider): MediaSession.SessionCallback {
        return SessionCallbackBuilder(this, sessionPlayerConnector)
                .setFastForwardIncrementMs(500)
                .setRewindIncrementMs(500)
                .setMediaItemProvider(mediaItemProvider)
                .build()
    }

    @Provides
    @MainScope
    fun MainActivity.providesMediaSession(player: SessionPlayerConnector, sessionCallback: MediaSession.SessionCallback): MediaSession {
        return MediaSession.Builder(this, player)
                .setSessionCallback(Executors.newSingleThreadExecutor(), sessionCallback)
                .build()
    }

    @Provides
    @MainScope
    fun MainActivity.providesMediaController(session: MediaSession): MediaController = MediaController.Builder(this)
            .setSessionToken(session.token)
            .build()

}