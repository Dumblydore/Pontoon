package me.mauricee.pontoon.preferences.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import dagger.android.support.AndroidSupportInjection
import me.mauricee.pontoon.R
import me.mauricee.pontoon.model.edge.EdgeRepository
import me.mauricee.pontoon.preferences.PreferencesNavigator
import me.mauricee.pontoon.preferences.accentColor.AccentColorPreference
import me.mauricee.pontoon.preferences.baseTheme.BaseThemePreference
import me.mauricee.pontoon.preferences.primaryColor.PrimaryColorPreference
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var navigator: PreferencesNavigator
    @Inject
    lateinit var edgeRepository: EdgeRepository

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        findPreference("settings_about").setOnPreferenceClickListener { navigator.toAbout(); true }
        findPreference("settings_refresh_edges").setOnPreferenceClickListener { edgeRepository.refresh(); true }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is BaseThemePreference -> bindFragment(BaseThemePreference.Fragment.newInstance(preference.key))
            is AccentColorPreference -> bindFragment(AccentColorPreference.Fragment.newInstance(preference.key))
            is PrimaryColorPreference -> bindFragment(PrimaryColorPreference.Fragment.newInstance(preference.key))
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun bindFragment(fragment: PreferenceDialogFragmentCompat) = fragment
            .also { it.setTargetFragment(this, 0) }
            .show(fragmentManager, "$DialogPrefix.BaseThemePreference")

    companion object {
        const val DialogPrefix = "androidx.preference.PreferenceCategory"
    }
}