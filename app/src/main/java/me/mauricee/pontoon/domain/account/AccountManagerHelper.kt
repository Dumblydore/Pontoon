package me.mauricee.pontoon.domain.account

import android.content.SharedPreferences
import android.service.autofill.UserData
import androidx.core.content.edit
import com.google.gson.Gson
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.User
import java.util.*
import javax.inject.Inject

@AppScope
class AccountManagerHelper @Inject constructor(private val sharedPreferences: SharedPreferences,
                                               private val gson: Gson) {

    var account: User
        get() = sharedPreferences.getString(UserData, "").let { gson.fromJson(it, User::class.java) }
        set(value) {
            sharedPreferences.edit { putString(UserData, gson.toJson(value)) }
        }

    val isLoggedIn: Boolean
        get() = sharedPreferences.contains(UserData)

    internal val cfduid = sharedPreferences.getString(CfDuid, UUID.randomUUID().toString()).also {
        sharedPreferences.edit { putString(CfDuid, it) }
    }

    internal var sid: String = sharedPreferences.getString(SailsSid, "")
        set(value) {
            if (field != value) {
                field = value
                sharedPreferences.edit { putString(SailsSid, value) }
            }
        }

    fun login(cfduid: String, sid: String) = sharedPreferences.edit(true) {
        putString(CfDuid, cfduid)
        putString(SailsSid, sid)
    }

    fun logout() = sharedPreferences.edit(true) {
        remove(CfDuid)
        remove(SailsSid)
        remove(UserData)
    }

    companion object {
        private const val SailsSid = "sails.sid"
        private const val CfDuid = "cfduid"
        private const val UserData: String = "com.floatplane"
    }
}