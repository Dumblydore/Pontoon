package me.mauricee.pontoon.ui.launch

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface LaunchModule {
    @LaunchScope
    @ContributesAndroidInjector
    fun contributesLaunchFragment(): LaunchFragment
}