package me.mauricee.pontoon.preferences

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.mauricee.pontoon.preferences.accentColor.AccentColorPreference
import me.mauricee.pontoon.preferences.baseTheme.BaseThemePreference
import me.mauricee.pontoon.preferences.primaryColor.PrimaryColorPreference
import me.mauricee.pontoon.preferences.settings.SettingsFragment

@Module
abstract class PreferenceModule {

    @Binds
    @PreferencesScope
    abstract fun bindPreferenceNavigator(preferencesActivity: PreferencesActivity): PreferencesNavigator

    @ContributesAndroidInjector
    abstract fun contrributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeBaseThemePreference(): BaseThemePreference.Fragment

    @ContributesAndroidInjector
    abstract fun contributePrimaryColorPreference(): PrimaryColorPreference.Fragment

    @ContributesAndroidInjector
    abstract fun contributeAccentColorPreference(): AccentColorPreference.Fragment
}