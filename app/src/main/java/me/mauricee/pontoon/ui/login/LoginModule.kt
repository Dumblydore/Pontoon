package me.mauricee.pontoon.ui.login

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.ui.login.login.LoginFragment
import me.mauricee.pontoon.ui.login.webLogin.WebLoginFragment

@Module
abstract class LoginModule {

    @Binds
    @LoginScope
    abstract fun bindLoginNavigator(activity: LoginActivity): LoginNavigator

    @Binds
    @LoginScope
    abstract fun bindLifecycleOwner(mainActivity: LoginActivity): LifecycleOwner

    @Binds
    @LoginScope
    abstract fun bindActivity(mainActivity: LoginActivity): AppCompatActivity

    @Binds
    @LoginScope
    abstract fun bindPage(activity: LoginActivity): EventTracker.Page

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeLttLoginFragment(): WebLoginFragment

    companion object {

        @Provides
        @LoginScope
        fun provideEventTracker(page: EventTracker.Page) = EventTracker(page)

        @Provides
        @LoginScope
        fun LoginActivity.providesViewModel(): LoginViewModel = loginViewModel
    }
}