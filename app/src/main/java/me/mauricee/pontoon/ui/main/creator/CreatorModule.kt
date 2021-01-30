package me.mauricee.pontoon.ui.main.creator

import dagger.Module
import dagger.Provides

@Module
object CreatorModule {
    @Provides
    fun CreatorFragment.provideArgs(): CreatorContract.Args = CreatorContract.Args(requireArguments().getString(CreatorFragment.CreatorIdKey)!!)
}