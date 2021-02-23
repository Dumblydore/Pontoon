package me.mauricee.pontoon.tv.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.paging.PagedList
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.util.Util
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.repository.util.AuthInterceptor
import me.mauricee.pontoon.tv.BuildConfig
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
@InstallIn(SingletonComponent::class)
interface TvModule {
    @Binds
    fun bindContext(application: Application): Context

    companion object {
        @Provides
        fun provideMoshi() = Moshi.Builder()
//            .add(InstantAdapter())
                .build()

        @Provides
        fun Moshi.provideConverterFactory() = MoshiConverterFactory.create(this)

        @Provides
        fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory =
                RxJava2CallAdapterFactory.createAsync()

        @Provides
        fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

        @Provides
        fun providesFloatPlaneApi(converterFactory: MoshiConverterFactory,
                                  authInterceptor: AuthInterceptor,
//                              firebaseNetworkInterceptor: FirebaseNetworkInterceptor,
                                  callFactory: RxJava2CallAdapterFactory, client: OkHttpClient): FloatPlaneApi = client.newBuilder().addInterceptor(authInterceptor)
                /*.addInterceptor(firebaseNetworkInterceptor)*/.build()
                .let { Retrofit.Builder().client(it) }
                .addConverterFactory(converterFactory)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(callFactory)
                .baseUrl("https://www.floatplane.com/api/")
                .build().create(FloatPlaneApi::class.java)

        @Provides
        fun providesSharedPreferences(context: Context): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)

        @Provides
        fun providesWifiManager(context: Context): WifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        @Provides
        fun providesPowerManager(context: Context): PowerManager =
                context.getSystemService(Context.POWER_SERVICE) as PowerManager

        @Provides
        fun providesUserAgent(context: Context): String = Util.getUserAgent(context, BuildConfig.APPLICATION_ID)

        @Provides
        fun providesDateFormatter() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())

        @Provides
        fun providesPageConfig() = PagedList.Config.Builder().setPageSize(20)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()

        @Provides
        fun providesSession(context: Context): MediaSessionCompat = MediaSessionCompat(context, BuildConfig.APPLICATION_ID)
    }
}