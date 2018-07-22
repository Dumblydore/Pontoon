package me.mauricee.pontoon.preferences.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.mauricee.pontoon.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

}