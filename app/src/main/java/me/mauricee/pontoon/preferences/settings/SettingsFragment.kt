package me.mauricee.pontoon.preferences.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import com.crashlytics.android.Crashlytics
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import me.mauricee.pontoon.BuildConfig
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.hasNotch
import me.mauricee.pontoon.ext.toast
import me.mauricee.pontoon.preferences.accentColor.AccentColorPreference
import me.mauricee.pontoon.preferences.primaryColor.PrimaryColorPreference
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), SettingsContract.View {

    override val actions: Observable<SettingsContract.Action>
        get() = actionRelay
    @Inject
    lateinit var presenter: SettingsPresenter

    private val actionRelay: Relay<SettingsContract.Action> = PublishRelay.create()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        findPreference<Preference>("settings_about")?.setOnPreferenceClickListener { push(SettingsContract.Action.SelectedAbout) }
        findPreference<Preference>("settings_privacy_policy")?.setOnPreferenceClickListener { push(SettingsContract.Action.SelectedPrivacyPolicy) }
        findPreference<Preference>("settings_refresh_edges")?.setOnPreferenceClickListener { push(SettingsContract.Action.SelectedRefreshEdges) }
        if (!requireActivity().hasNotch()) {
            (findPreference<Preference>("settings_general") as PreferenceCategory).removePreference(findPreference("settings_notch"))
        }
        if (!BuildConfig.DEBUG) {
            (findPreference<Preference>("settings_general") as PreferenceCategory).removePreference(findPreference("settings_test_crash"))
        } else {
            findPreference<Preference>("settings_test_crash")?.setOnPreferenceClickListener { Crashlytics.getInstance().crash(); true }
        }

        presenter.attachView(this)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is AccentColorPreference -> push(SettingsContract.Action.OpenAccentColorPreference(preference.key))
            is PrimaryColorPreference -> push(SettingsContract.Action.OpenPrimaryColorPreference(preference.key))
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun updateState(state: SettingsContract.State) = when (state) {
        SettingsContract.State.RefreshingEdges -> toast("Refreshing...")
        SettingsContract.State.RefreshedEdges -> toast("Edges Refreshed!")
        SettingsContract.State.ErrorRefreshingEdges -> toast("Error Refreshing Edges!")
        is SettingsContract.State.DisplayAccentColorPreference -> bindFragment(AccentColorPreference.Fragment.newInstance(state.key))
        is SettingsContract.State.DisplayPrimaryColorPreference -> bindFragment(PrimaryColorPreference.Fragment.newInstance(state.key))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    private fun bindFragment(fragment: PreferenceDialogFragmentCompat) = fragment
            .also { it.setTargetFragment(this, 0) }
            .show(requireFragmentManager(), "$DialogPrefix.ThemePreference")

    companion object {
        const val DialogPrefix = "androidx.preference.PreferenceCategory"
    }

    private fun push(action: SettingsContract.Action): Boolean {
        actionRelay.accept(action)
        return true
    }
}