package me.mauricee.pontoon.ui.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mikepenz.aboutlibraries.LibsBuilder
import me.mauricee.pontoon.ui.BaseActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.ext.loadFragment
import me.mauricee.pontoon.ui.preferences.settings.SettingsFragment

class PreferencesActivity : BaseActivity(), PreferencesNavigator, EventTracker.Page {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)
        loadFragment { replace(R.id.preference_container, SettingsFragment()) }
    }

    override fun toPrivacyPolicy() {
        startActivity(Intent(Intent.ACTION_VIEW, PrivacyManager.privacyPolicyUri))
    }

    override fun toAbout() {
        loadFragment { replace(R.id.preference_container, LibsBuilder().supportFragment()) }
    }

    companion object {
        fun navigateTo(context: Context) = context.startActivity(Intent(context, PreferencesActivity::class.java))
    }
}
