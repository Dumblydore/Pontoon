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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.isupatches.wisefy.WiseFy
import com.vanniktech.rxpermission.RealRxPermission
import com.vanniktech.rxpermission.RxPermission
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.analytics.FirebaseNetworkInterceptor
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
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
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

    companion object {

        @AppScope
        @Provides
        fun providesSharedPreferences(context: Context): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

        @AppScope
        @Provides
        fun providesAccountManager(context: Context) = AccountManager.get(context)

        @Provides
        @AppScope
        fun providesWifiManager(context: Context): WifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        @Provides
        @AppScope
        fun providesPowerManager(context: Context): PowerManager =
                context.getSystemService(Context.POWER_SERVICE) as PowerManager

        @Provides
        @AppScope
        fun providesUserAgent(context: Context): String =
                Util.getUserAgent(context, BuildConfig.APPLICATION_ID)

        @AppScope
        @Provides
        fun providesGson(): Gson = ThreeTenGsonAdapter.registerAll(GsonBuilder().setLenient()).create()

        @AppScope
        @Provides
        fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory =
                RxJava2CallAdapterFactory.createAsync()

        @AppScope
        @Provides
        fun providesGsonConverterFactory(gson: Gson): GsonConverterFactory =
                GsonConverterFactory.create(gson)

        @AppScope
        @Provides
        fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

        @AppScope
        @Provides
        fun providesFloatPlaneApi(converterFactory: GsonConverterFactory,
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
        fun providesHlsFactory(okHttpClient: OkHttpClient, authInterceptor: AuthInterceptor, agent: String) =
                HlsMediaSource.Factory(OkHttpDataSourceFactory(okHttpClient.newBuilder().addInterceptor(authInterceptor).build()::newCall, agent))

        @AppScope
        @Provides
        fun providesDateFormatter() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())

        @AppScope
        @Provides
        fun providesPageConfig() = PagedList.Config.Builder().setPageSize(20)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()

        @Provides
        @AppScope
        fun providesSession(context: Context): MediaSessionCompat =
                MediaSessionCompat(context, "Pontoon")

        @Provides
        @AppScope
        fun providesWiseFy(context: Context): WiseFy = WiseFy.Brains(context).getSmarts()

        @Provides
        @AppScope
        fun providesRxPermission(context: Context): RxPermission = RealRxPermission.getInstance(context)

        @Provides
        @AppScope
        fun providesAudioAttributes() = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()

        @Provides
        @AppScope
        fun providesLocalExoPlayer(audioAttributes: AudioAttributes, context: Context) =
                ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
                        .apply {
                            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                            setAudioAttributes(audioAttributes, true)
                        }
    }
}