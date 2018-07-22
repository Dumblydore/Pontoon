package me.mauricee.pontoon.login.login

import dagger.Binds
import dagger.Module

@Module
interface LoginModule {
    @Binds
    fun providePresenter(presenter: LoginPresenter): LoginContract.Presenter
}