package me.mauricee.pontoon.di

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.paging.PagedList
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.BuildConfig
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
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.preferences.PreferenceModule
import me.mauricee.pontoon.preferences.PreferencesActivity
import me.mauricee.pontoon.preferences.PreferencesScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    @Module
    companion object {

        @AppScope
        @Provides
        @JvmStatic
        fun providesSharedPreferences(context: Context): SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
//                context.getSharedPreferences("pontoonSharedPrefs", MODE_PRIVATE)

        @AppScope
        @Provides
        @JvmStatic
        fun providesAccountManager(context: Context) = AccountManager.get(context)

        @Provides
        @AppScope
        @JvmStatic
        fun providesSession(context: Context): MediaSessionCompat {
            return MediaSessionCompat(context, "MusicService")
        }

        @Provides
        @AppScope
        @JvmStatic
        fun providesWifiManager(context: Context): WifiManager {
            return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        }

        @Provides
        @AppScope
        @JvmStatic
        fun providesPowerManager(context: Context): PowerManager {
            return context.getSystemService(Context.POWER_SERVICE) as PowerManager
        }

        @Provides
        @AppScope
        @JvmStatic
        fun providesAudioManager(context: Context): AudioManager {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

        @Provides
        @AppScope
        @JvmStatic
        fun providesUserAgent(context: Context): String {
            return Util.getUserAgent(context, BuildConfig.APPLICATION_ID)
        }

        @AppScope
        @Provides
        @JvmStatic
        fun providesGson(): Gson = ThreeTenGsonAdapter.registerAll(GsonBuilder().setLenient()).create()

        @AppScope
        @Provides
        @JvmStatic
        fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory =
                RxJava2CallAdapterFactory.createAsync()

        @AppScope
        @Provides
        @JvmStatic
        fun providesGsonConverterFactory(gson: Gson): GsonConverterFactory =
                GsonConverterFactory.create(gson)

        @AppScope
        @Provides
        @JvmStatic
        fun providesHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .also {
                    if (BuildConfig.DEBUG) {
                        it.addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    }
                }
                .build()


        @AppScope
        @Provides
        @JvmStatic
        fun providesFloatPlaneApi(converterFactory: GsonConverterFactory,
                                  authInterceptor: AuthInterceptor,
                                  callFactory: RxJava2CallAdapterFactory, client: OkHttpClient):
                FloatPlaneApi = client.newBuilder().addInterceptor(authInterceptor).build()
                .let { Retrofit.Builder().client(it) }
                .addConverterFactory(converterFactory)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(callFactory)
                .baseUrl("https://www.floatplane.com/api/")
                .build().create(FloatPlaneApi::class.java)

        @AppScope
        @Provides
        @JvmStatic
        fun providesDatabase(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb").build()

        @AppScope
        @Provides
        @JvmStatic
        fun providesUserDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.userDao

        @AppScope
        @Provides
        @JvmStatic
        fun providesCreatorDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.creatorDao

        @AppScope
        @Provides
        @JvmStatic
        fun providesVideoDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.videoDao

        @AppScope
        @Provides
        @JvmStatic
        fun providesHistoryDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.historyDao

        @AppScope
        @Provides
        @JvmStatic
        fun providesCommentDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.commentDao

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
    }
}