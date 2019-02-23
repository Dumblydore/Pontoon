package me.mauricee.pontoon.analytics

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleObserver
import io.reactivex.Observable
import me.mauricee.pontoon.R
import me.mauricee.pontoon.rx.preferences.watchBoolean
import javax.inject.Inject

class PrivacyManager @Inject constructor(private val sharedPreferences: SharedPreferences) : LifecycleObserver {

    val isAnalyticsEnabled: Observable<Boolean>
        get() = sharedPreferences.watchBoolean(AnalyticsEnabledKey, false)
                .startWith(sharedPreferences.getBoolean(AnalyticsEnabledKey, false))

    private var hasUserBeenPrompted: Boolean
        get() = sharedPreferences.getBoolean(AnalyticsPromptKey, false)
        set(value) = sharedPreferences.edit(true) { putBoolean(AnalyticsPromptKey, true) }

    private fun revokeAnalytics() {
        sharedPreferences.edit {
            putBoolean(AnalyticsEnabledKey, false)
            putBoolean(AnalyticsPromptKey, true)
        }
        dialog = null
    }

    private fun allowAnalytics() {
        sharedPreferences.edit {
            putBoolean(AnalyticsEnabledKey, true)
            putBoolean(AnalyticsPromptKey, true)
        }
        dialog = null
    }

    private var dialog: AlertDialog? = null
    fun displayPromptIfUserHasNotBeenPrompted(activity: Activity) {
        if (!hasUserBeenPrompted) {
            dialog = AlertDialog.Builder(activity)
                    .setTitle(R.string.analytics_prompt_title)
                    .setMessage(R.string.analytics_prompt_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.analytics_prompt_positive) { a, b -> allowAnalytics() }
                    .setNegativeButton(R.string.analytics_prompt_negative) { a, b -> revokeAnalytics() }
                    .setNeutralButton(R.string.analytics_prompt_neutral) { a, b -> activity.startActivity(Intent(Intent.ACTION_VIEW, privacyPolicyUri)) }
                    .create().apply { show() }
        }
    }

    fun hidePromptIfOpen() {
        dialog?.hide()
    }

    companion object {
        private const val AnalyticsEnabledKey = "settings_analytics"
        private const val AnalyticsPromptKey = "AnalyticsPrompt"
        val privacyPolicyUri = "https://github.com/Dumblydore/Pontoon/blob/master/PRIVACY".toUri()
    }
}