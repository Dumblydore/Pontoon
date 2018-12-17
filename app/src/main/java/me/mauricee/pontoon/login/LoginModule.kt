package me.mauricee.pontoon.login

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.login.login.LoginFragment
import me.mauricee.pontoon.login.webLogin.WebLoginFragment

@Module
abstract class LoginModule {

    @Binds
    @LoginScope
    abstract fun bindLoginNavigator(activity: LoginActivity): LoginNavigator

    @Binds
    @LoginScope
    abstract fun bindPage(activity: LoginActivity): EventTracker.Page

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeLttLoginFragment(): WebLoginFragment

    @Module
    companion object {

        @Provides
        @LoginScope
        fun provideEventTracker(page: EventTracker.Page) = EventTracker(page)
    }
}