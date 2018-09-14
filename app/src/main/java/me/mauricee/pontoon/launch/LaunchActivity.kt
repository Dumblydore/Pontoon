package me.mauricee.pontoon.launch

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjection
import io.fabric.sdk.android.Fabric
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.Style
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.main.MainActivity
import javax.inject.Inject

class LaunchActivity : AppCompatActivity() {

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper
    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        @StyleRes val launcherTheme = when(themeManager.style) {
            is Style.Light -> R.style.AppTheme_Light_Launcher
            is Style.Black -> R.style.AppTheme_Light_Launcher
            is Style.Dark -> R.style.AppTheme_Light_Launcher
        }
        setTheme(launcherTheme)
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        val act = if (accountManagerHelper.isLoggedIn) MainActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, act))
    }
}