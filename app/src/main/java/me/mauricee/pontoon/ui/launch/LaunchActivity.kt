package me.mauricee.pontoon.ui.launch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dagger.android.AndroidInjection
import me.mauricee.pontoon.R
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.with
import me.mauricee.pontoon.ui.login.LoginActivity
import me.mauricee.pontoon.ui.main.MainActivity
import javax.inject.Inject

class LaunchActivity : AppCompatActivity() {

    @Inject
    lateinit var accountManagerHelper: AccountManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        val url = intent.data
        val path = url?.path
        //Wonder if it makes sense to pass the Video to play to login if user isn't logged in.
        when {
            path?.contains(activationPath) == true -> url.with { LoginActivity.activate(it.getQueryParameter(keyQuery)!!, it.getQueryParameter(usernameQuery)!!, this) }
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