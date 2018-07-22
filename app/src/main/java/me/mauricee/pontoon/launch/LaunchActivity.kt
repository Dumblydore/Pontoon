package me.mauricee.pontoon.launch

import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.main.MainActivity
import javax.inject.Inject

class LaunchActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Launcher)
        super.onCreate(savedInstanceState)
        (if (accountManagerHelper.isLoggedIn) MainActivity::class.java else LoginActivity::class.java)
                .let { startActivity(Intent(this, it)) }

    }
}