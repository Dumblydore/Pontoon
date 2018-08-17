package me.mauricee.pontoon.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import me.mauricee.pontoon.EventTracker
import me.mauricee.pontoon.R
import me.mauricee.pontoon.main.MainActivity

class LoginActivity : DaggerAppCompatActivity(), LoginNavigator, EventTracker.Page {

    override val trackerTag: String
        get() = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun toSubscriptions() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    companion object {
        fun navigateTo(context: Context) {
            Intent(context, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let(context::startActivity)
        }
    }
}