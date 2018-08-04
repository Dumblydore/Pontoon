package me.mauricee.pontoon.common

import android.content.SharedPreferences
import androidx.core.content.edit
import me.mauricee.pontoon.di.AppScope
import org.threeten.bp.LocalTime
import javax.inject.Inject

class CacheValidator(private val key: String, private val sharedPreferences: SharedPreferences) {

    fun <T> refresh(action: () -> T): T {
        this.sharedPreferences.edit { putLong(key, LocalTime.now().toNanoOfDay()) }
        return action()
    }

    fun <T> check(cacheIsActive: () -> T, onCacheInactive: () -> T): T {
        return if (sharedPreferences.getLong(key, LocalTime.now().toNanoOfDay()) > 0) cacheIsActive()
        else refresh(onCacheInactive)
    }

    @AppScope
    class Factory @Inject constructor(private val sharedPreferences: SharedPreferences) {
        fun newInstance(key: String) = CacheValidator(key, sharedPreferences)
    }
}