package me.mauricee.pontoon.domain.account

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import me.mauricee.pontoon.domain.floatplane.UserJson
import javax.inject.Inject

@Deprecated(message = "Use Session repository")
open class AccountManagerHelper @Inject constructor(private val sharedPreferences: SharedPreferences,
                                                    private val gson: Gson) {

    var account: UserJson
        get() = sharedPreferences.getString(UserData, "").let { gson.fromJson(it, UserJson::class.java) }
        set(value) {
            sharedPreferences.edit(commit = true) { putString(UserData, gson.toJson(value)) }
        }

    companion object {
        private const val SailsSid = "sails.sid"
        private const val CfDuid = "cfduid"
        private const val UserData: String = "com.floatplane"
    }
}