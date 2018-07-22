package me.mauricee.pontoon.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.loadFragment
import me.mauricee.pontoon.preferences.settings.SettingsFragment

class PreferencesActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)
        loadFragment {
            replace(R.id.preference_container, SettingsFragment())
        }
    }

    companion object {
        fun navigateTo(context: Context) = context.startActivity(Intent(context, PreferencesActivity::class.java))
    }
}
