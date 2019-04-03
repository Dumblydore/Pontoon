package me.mauricee.pontoon.domain.account

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.User
import java.util.*
import javax.inject.Inject

@AppScope
open class AccountManagerHelper @Inject constructor(private val sharedPreferences: SharedPreferences,
                                                    moshi: Moshi) {

    private val userAdapter: JsonAdapter<User> = moshi.adapter(User::class.java)

    var account: User
        get() = sharedPreferences.getString(UserData, "").let { userAdapter.fromJson(it!!)!! }
        set(value) {
            sharedPreferences.edit(commit = true) { putString(UserData, userAdapter.toJson(value)) }
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

    internal var sid: String = sharedPreferences.getString(SailsSid, "")
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