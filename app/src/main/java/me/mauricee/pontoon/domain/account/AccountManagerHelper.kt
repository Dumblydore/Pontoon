package me.mauricee.pontoon.domain.account

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.UserJson
import java.util.*
import javax.inject.Inject

@AppScope
@Deprecated(message = "Use Session repository")
open class AccountManagerHelper @Inject constructor(private val sharedPreferences: SharedPreferences,
                                                    private val gson: Gson) {

    var account: UserJson
        get() = sharedPreferences.getString(UserData, "").let { gson.fromJson(it, UserJson::class.java) }
        set(value) {
            sharedPreferences.edit(commit = true) { putString(UserData, gson.toJson(value)) }
        }

    val isLoggedIn: Boolean
        get() = sharedPreferences.contains(UserData)

    internal var cfduid = sharedPreferences.getString(CfDuid, UUID.randomUUID().toString())
            .also { sharedPreferences.edit { putString(CfDuid, it) } }
        private set(value) {
            if (field != value) {
                sharedPreferences.edit(true) { putString(CfDuid, value) }
            }
        }

    internal var sid: String = sharedPreferences.getString(SailsSid, "")!!
        set(value) {
            if (field != value) {
                field = value
                sharedPreferences.edit(true) { putString(SailsSid, value) }
            }
        }

    fun login(cfduid: String, sid: String) {
        this.cfduid = cfduid
        this.sid = sid
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