package me.mauricee.pontoon.login

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.login.login.LoginFragment
import me.mauricee.pontoon.login.login.LoginModule

@Module
abstract class LoginModule {

    @Binds
    abstract fun bindLoginNavigator(activity: LoginActivity): LoginNavigator

    @ContributesAndroidInjector(modules = [LoginModule::class])
    abstract fun contributeLoginFragment(): LoginFragment
}