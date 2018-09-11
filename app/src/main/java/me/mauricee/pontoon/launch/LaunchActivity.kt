package me.mauricee.pontoon.launch

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager
import me.mauricee.pontoon.BaseActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.main.MainActivity
import javax.inject.Inject

class LaunchActivity : BaseActivity() {

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.AppTheme_Launcher)
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        val act = if (accountManagerHelper.isLoggedIn) MainActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, act))
    }
}