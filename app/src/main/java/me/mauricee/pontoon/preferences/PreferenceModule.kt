package me.mauricee.pontoon.preferences

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.preferences.settings.SettingsFragment

@Module
abstract class PreferenceModule {

    @Binds
    @PreferencesScope
    abstract fun bindPreferenceNavigator(preferencesActivity: PreferencesActivity): PreferencesNavigator



    @ContributesAndroidInjector
    abstract fun contrributeSettingsFragment(): SettingsFragment
}