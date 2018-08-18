package me.mauricee.pontoon.preferences.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import me.mauricee.pontoon.R
import me.mauricee.pontoon.preferences.PreferencesNavigator
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var navigator: PreferencesNavigator

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        findPreference("settings_about").setOnPreferenceClickListener { navigator.toAbout(); true }
    }


}