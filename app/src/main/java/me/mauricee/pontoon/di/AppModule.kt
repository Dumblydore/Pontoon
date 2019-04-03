package me.mauricee.pontoon.di

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.paging.PagedList
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import com.isupatches.wisefy.WiseFy
import com.squareup.moshi.Moshi
import com.vanniktech.rxpermission.RealRxPermission
import com.vanniktech.rxpermission.RxPermission
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.analytics.FirebaseNetworkInterceptor
import me.mauricee.pontoon.common.moshi.Converters
import me.mauricee.pontoon.common.moshi.ListJsonAdapter
import me.mauricee.pontoon.domain.floatplane.AuthInterceptor
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.launch.LaunchActivity
import me.mauricee.pontoon.launch.LaunchScope
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.login.LoginModule
import me.mauricee.pontoon.login.LoginScope
import me.mauricee.pontoon.main.MainActivity
import me.mauricee.pontoon.main.MainModule
import me.mauricee.pontoon.main.MainScope
import me.mauricee.pontoon.preferences.PreferenceModule
import me.mauricee.pontoon.preferences.PreferencesActivity
import me.mauricee.pontoon.preferences.PreferencesScope
import okhttp3.OkHttpClient
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

@Module
abstract class AppModule {

    @Binds
    abstract fun bindContext(application: Application): Context

    @LaunchScope
    @ContributesAndroidInjector
    abstract fun contributeLaunchActivity(): LaunchActivity

    @LoginScope
    @ContributesAndroidInjector(modules = [LoginModule::class])
    abstract fun contributeLoginActivity(): LoginActivity

    @MainScope
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @PreferencesScope
    @ContributesAndroidInjector(modules = [PreferenceModule::class])
    abstract fun contributePreferenceActivity(): PreferencesActivity


    @Module
    companion object {

        @AppScope
        @Provides
        @JvmStatic
        fun providesSharedPreferences(context: Context): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

        @AppScope
        @Provides
        @JvmStatic
        fun providesAccountManager(context: Context) = AccountManager.get(context)

        @Provides
        @AppScope
        @JvmStatic
        fun providesWifiManager(context: Context): WifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        @Provides
        @AppScope
        @JvmStatic
        fun providesPowerManager(context: Context): PowerManager =
                context.getSystemService(Context.POWER_SERVICE) as PowerManager

        @Provides
        @AppScope
        @JvmStatic
        fun providesUserAgent(context: Context): String =
                Util.getUserAgent(context, BuildConfig.APPLICATION_ID)

        @AppScope
        @Provides
        @JvmStatic
        fun providesMoshi(): Moshi = Moshi.Builder()
                .add(Converters())
                .add(ListJsonAdapter.Factory)
                .build()

        @AppScope
        @Provides
        @JvmStatic
        fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory =
                RxJava2CallAdapterFactory.createAsync()

        @AppScope
        @Provides
        @JvmStatic
        fun providesMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory =
                MoshiConverterFactory.create(moshi).asLenient()

        @AppScope
        @Provides
        @JvmStatic
        fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

        @AppScope
        @Provides
        @JvmStatic
        fun providesFloatPlaneApi(converterFactory: MoshiConverterFactory,
                                  authInterceptor: AuthInterceptor,
                                  firebaseNetworkInterceptor: FirebaseNetworkInterceptor,
                                  callFactory: RxJava2CallAdapterFactory, client: OkHttpClient):
                FloatPlaneApi = client.newBuilder().addInterceptor(authInterceptor)
                .addInterceptor(firebaseNetworkInterceptor).build()
                .let { Retrofit.Builder().client(it) }
                .addConverterFactory(converterFactory)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(callFactory)
                .baseUrl("https://www.floatplane.com/api/")
                .build().create(FloatPlaneApi::class.java)

        @AppScope
        @Provides
        @JvmStatic
        fun providesHlsFactory(okHttpClient: OkHttpClient, agent: String) =
                HlsMediaSource.Factory(OkHttpDataSourceFactory(okHttpClient::newCall, agent))

        @AppScope
        @Provides
        @JvmStatic
        fun providesDateFormatter() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())

        @AppScope
        @Provides
        @JvmStatic
        fun providesPageConfig() = PagedList.Config.Builder().setPageSize(20)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()

        @Provides
        @AppScope
        @JvmStatic
        fun providesSession(context: Context): MediaSessionCompat =
                MediaSessionCompat(context, "Pontoon")

        @Provides
        @AppScope
        @JvmStatic
        fun providesWiseFy(context: Context): WiseFy = WiseFy.Brains(context).getSmarts()

        @Provides
        @AppScope
        @JvmStatic
        fun providesRxPermission(context: Context): RxPermission = RealRxPermission.getInstance(context)

        @Provides
        @AppScope
        @JvmStatic
        fun providesAudioAttributes() = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()

        @Provides
        @AppScope
        @JvmStatic
        fun providesLocalExoPlayer(audioAttributes: AudioAttributes, context: Context) =
                ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
                        .apply {
                            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                            setAudioAttributes(audioAttributes, true)
                        }
    }
}