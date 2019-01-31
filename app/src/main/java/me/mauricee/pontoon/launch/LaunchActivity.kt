package me.mauricee.pontoon.launch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.google.firebase.perf.metrics.AddTrace
import dagger.android.AndroidInjection
import io.fabric.sdk.android.Fabric
import me.mauricee.pontoon.R
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.main.MainActivity
import javax.inject.Inject

class LaunchActivity : AppCompatActivity() {

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    @AddTrace(name = "SplashScreen")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setTheme(R.style.AppTheme_Launcher)
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        val url = intent.data
        val path = url?.path
        //Wonder if it makes sense to pass the Video to play to login if user isn't logged in.
        when {
            path?.contains(activationPath) == true -> url.with { LoginActivity.activate(it.getQueryParameter(keyQuery), it.getQueryParameter(usernameQuery), this) }
            path?.contains(videoPath) == true && accountManagerHelper.isLoggedIn -> MainActivity.playVideo(this, intent.data!!.pathSegments.last())
            accountManagerHelper.isLoggedIn -> MainActivity.navigateTo(this)
            else -> LoginActivity.navigateTo(this)
        }
        finish()
    }

    companion object {
        const val videoPath: String = "video"
        const val activationPath: String = "activate-account"
        const val keyQuery: String = "key"
        const val usernameQuery: String = "username"

    }
}