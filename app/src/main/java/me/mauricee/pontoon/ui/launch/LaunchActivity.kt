package me.mauricee.pontoon.ui.launch

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dagger.android.AndroidInjection
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ui.login.LoginActivity
import me.mauricee.pontoon.ui.main.MainActivity
import javax.inject.Inject

class LaunchActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: LaunchViewModel.Factory
    private val viewModel: LaunchViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        viewModel.events.observe(this) {
            when (it) {
                is LaunchEvent.ToLogin -> LoginActivity.navigateTo(this)
                LaunchEvent.ToSession -> MainActivity.navigateTo(this)
            }
        }
    }
}