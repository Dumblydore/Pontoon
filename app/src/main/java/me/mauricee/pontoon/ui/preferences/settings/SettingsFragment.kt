package me.mauricee.pontoon.ui.preferences.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.ext.hasNotch
import me.mauricee.pontoon.ui.preferences.accentColor.AccentColorPreference
import me.mauricee.pontoon.ui.preferences.primaryColor.PrimaryColorPreference
import me.mauricee.pontoon.ui.preferences.settings.SettingsFragmentDirections.actionSettingsFragmentToAboutFragment

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: SettingsContract.ViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        findPreference<Preference>("settings_about")?.setOnPreferenceClickListener { push(SettingsContract.Action.SelectedAbout) }
        findPreference<Preference>("settings_privacy_policy")?.setOnPreferenceClickListener { push(SettingsContract.Action.SelectedPrivacyPolicy) }
//        findPreference<Preference>("settings_refresh_edges")?.setOnPreferenceClickListener { push(SettingsContract.Action.SelectedRefreshEdges) }
        if (!requireActivity().hasNotch()) {
            (findPreference<PreferenceCategory>("settings_general"))?.removePreference(findPreference("settings_notch"))
        }
        if (!BuildConfig.DEBUG) {
            (findPreference<PreferenceCategory>("settings_general"))?.removePreference(findPreference("settings_test_crash"))
        } else {
            findPreference<Preference>("settings_test_crash")?.setOnPreferenceClickListener { throw RuntimeException("Test!"); }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                SettingsContract.Event.NavigateToAbout -> findNavController().navigate(actionSettingsFragmentToAboutFragment())
                SettingsContract.Event.NavigateToPrivacyPolicy -> requireActivity().startActivity(Intent(Intent.ACTION_VIEW, PrivacyManager.privacyPolicyUri))
                is SettingsContract.Event.DisplayAccentColorPreference -> {
                    AccentColorPreference.Fragment.newInstance(event.key).also {
                        it.setTargetFragment(this, 0)
                    }.showNow(parentFragmentManager, "")
                }
                is SettingsContract.Event.DisplayPrimaryColorPreference -> {
                    PrimaryColorPreference.Fragment.newInstance(event.key).also {
                        it.setTargetFragment(this, 0)
                    }.showNow(parentFragmentManager, "")
                }
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is AccentColorPreference -> push(SettingsContract.Action.OpenAccentColorPreference(preference.key))
            is PrimaryColorPreference -> push(SettingsContract.Action.OpenPrimaryColorPreference(preference.key))
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun push(action: SettingsContract.Action): Boolean {
        viewModel.sendAction(action)
        return true
    }
}
