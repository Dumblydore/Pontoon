package me.mauricee.pontoon.ui.start

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.FirebaseApp

class FirebaseInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        FirebaseApp.initializeApp(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}