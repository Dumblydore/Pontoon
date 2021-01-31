package me.mauricee.pontoon.ui.main.user

import dagger.Module
import dagger.Provides

@Module
object UserModule {
    @Provides
    fun UserFragment.providesArgs(): UserArgs = UserArgs(requireArguments().getString(UserFragment.UserKey)!!)
}