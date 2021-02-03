package me.mauricee.pontoon.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.ActivityMainNewBinding
import me.mauricee.pontoon.ext.view.viewBinding
import javax.inject.Inject


@AndroidEntryPoint
class NewMainActivity : AppCompatActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    private val binding by viewBinding(ActivityMainNewBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        themeManager.onCreate(this)
        setContentView(binding.root)
    }
}