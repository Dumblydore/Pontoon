package me.mauricee.pontoon.domain.account

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.User
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

    fun logout() = sharedPreferences.edit{remove(UserData)}

    companion object {

        const val AccountType: String = "me.mauricee.pontoon"
        const val UserData: String = "com.floatplane"
    }

}