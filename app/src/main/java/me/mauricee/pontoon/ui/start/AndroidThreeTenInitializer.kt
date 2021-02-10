package me.mauricee.pontoon.ui.start

import android.content.Context
import androidx.startup.Initializer
import com.jakewharton.threetenabp.AndroidThreeTen

class AndroidThreeTenInitializer : Initializer<Unit> {
    override fun create(context: Context) = AndroidThreeTen.init(context)

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}