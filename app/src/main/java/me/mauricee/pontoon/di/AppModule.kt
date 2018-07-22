package me.mauricee.pontoon.di

import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import androidx.paging.PagedList
import androidx.room.Room
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
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
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.PontoonDatabase
import me.mauricee.pontoon.preferences.PreferencesActivity
import me.mauricee.pontoon.preferences.settings.PreferencesScope
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
import javax.inject.Named

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
    @ContributesAndroidInjector
    abstract fun contributePreferenceActivity(): PreferencesActivity

    @Module
    companion object {

        @AppScope
        @Provides
        @JvmStatic
        fun sharedPreferences(context: Context): SharedPreferences =
                context.getSharedPreferences("pontoonSharedPrefs", MODE_PRIVATE)

        @AppScope
        @Provides
        @JvmStatic
        fun accountManager(context: Context) = AccountManager.get(context)

        @Provides
        @AppScope
        @JvmStatic
        fun providesSession(context: Context): MediaSessionCompat {
            return MediaSessionCompat(context, "MusicService")
        }

        @Provides
        @AppScope
        @JvmStatic
        fun providesAudioManager(context: Context): AudioManager {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

        @AppScope
        @Provides
        @JvmStatic
        fun player(okHttpClient: OkHttpClient,
                   session: MediaSessionCompat,
                   audioManager: AudioManager,
                   context: Context): Player {
            val agent = Util.getUserAgent(context, BuildConfig.APPLICATION_ID)
            return Player(ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector()),
                    OkHttpDataSourceFactory(okHttpClient, agent, null),
                    DefaultExtractorsFactory(), audioManager, session)
        }

        @AppScope
        @Provides
        @JvmStatic
        fun gson(): Gson = ThreeTenGsonAdapter.registerAll(GsonBuilder().setLenient()).create()

        @AppScope
        @Provides
        @JvmStatic
        fun rxJavaCallAdapterFactory(): RxJava2CallAdapterFactory =
                RxJava2CallAdapterFactory.createAsync()

        @AppScope
        @Provides
        @JvmStatic
        fun gsonConverterFactory(gson: Gson): GsonConverterFactory =
                GsonConverterFactory.create(gson)

        @AppScope
        @Provides
        @JvmStatic
        fun httpClient(): OkHttpClient = OkHttpClient.Builder()
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
        fun floatPlaneApi(converterFactory: GsonConverterFactory,
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
        fun database(context: Context) = Room.databaseBuilder(context, PontoonDatabase::class.java, "pontoondb").build()

        @AppScope
        @Provides
        @JvmStatic
        fun userDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.userDao

        @AppScope
        @Provides
        @JvmStatic
        fun creatorDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.creatorDao

        @AppScope
        @Provides
        @JvmStatic
        fun videoDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.videoDao

        @AppScope
        @Provides
        @JvmStatic
        fun historyDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.historyDao

        @AppScope
        @Provides
        @JvmStatic
        fun commentDao(pontoonDatabase: PontoonDatabase) = pontoonDatabase.commentDao

        @AppScope
        @Provides
        @JvmStatic
        fun provideDateFormatter() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())

        @AppScope
        @Provides
        @JvmStatic
        fun pageConfig() = PagedList.Config.Builder().setPageSize(20)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()
    }
}