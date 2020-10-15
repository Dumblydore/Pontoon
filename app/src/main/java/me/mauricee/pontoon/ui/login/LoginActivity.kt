package me.mauricee.pontoon.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.net.toUri
import com.ncapdevi.fragnav.FragNavController
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.ui.BaseActivity
import me.mauricee.pontoon.ui.login.login.LoginFragment
import me.mauricee.pontoon.ui.login.webLogin.WebLoginFragment
import me.mauricee.pontoon.ui.main.MainActivity
import javax.inject.Inject

class LoginActivity : BaseActivity(), LoginNavigator, EventTracker.Page {

    @Inject
    lateinit var privacyManager: PrivacyManager

    @Inject
    lateinit var loginViewModelFactory: LoginViewModel.Factory

    val loginViewModel: LoginViewModel by viewModels { loginViewModelFactory }

    override val name: String
        get() = "Login"
    private val loginFragment: LoginFragment by lazy { LoginFragment.newInstance(activation, username) }
    private val activation: String
        get() = if (intent.hasExtra(ActivationKey)) intent.getStringExtra(ActivationKey)!! else ""
    private val username: String
        get() = if (intent.hasExtra(UsernameKey)) intent.getStringExtra(UsernameKey)!! else ""

    private lateinit var controller: FragNavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        controller = FragNavController.Builder(savedInstanceState, supportFragmentManager, R.id.login)
                .rootFragment(loginFragment)
                .build()
        loginViewModel.watchStateValue { prompt2FaCode }.observe(this) {
            if (it) controller.clearStack()
        }
    }

    override fun onBackPressed() {
        if (controller.isRootFragment)
            super.onBackPressed()
        else
            controller.popFragment()
    }

    override fun onStart() {
        super.onStart()
        privacyManager.displayPromptIfUserHasNotBeenPrompted(this)
    }

    override fun onStop() {
        super.onStop()
        privacyManager.hidePromptIfOpen()
    }

    override fun toLttLogin() = controller.pushFragment(WebLoginFragment.loginWithLttForum())

    override fun toDiscordLogin() = controller.pushFragment(WebLoginFragment.loginWithDiscord())

    override fun toSignUp() = startActivity(Intent(Intent.ACTION_VIEW, SignupUrl.toUri()))

    override fun toPrivacyPolicy() = startActivity(Intent(Intent.ACTION_VIEW, PrivacyManager.privacyPolicyUri))

    override fun onSuccessfulLogin() {
        MainActivity.navigateTo(this)
        finish()
    }

    companion object {
        private const val SignupUrl = "https://www.floatplane.com/signup"
        private const val ActivationKey = "activation"
        private const val UsernameKey = "userName"

        fun navigateTo(context: Context) {
            Intent(context, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
                            and Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .let(context::startActivity)
        }

        fun activate(key: String, username: String, context: Context) {
            Intent(context, LoginActivity::class.java)
                    .putExtra(ActivationKey, key)
                    .putExtra(UsernameKey, username)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
                            and Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .let(context::startActivity)
        }
    }
}