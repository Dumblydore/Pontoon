package me.mauricee.pontoon.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.mauricee.pontoon.BaseActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.main.MainActivity

class LoginActivity : BaseActivity(), LoginNavigator, EventTracker.Page {

    override val name: String
        get() = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun toSubscriptions() {
        MainActivity.navigateTo(this)
        finish()
    }

    companion object {
        fun navigateTo(context: Context) {
            Intent(context, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
                            and Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .let(context::startActivity)
        }
    }
}