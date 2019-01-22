package me.mauricee.pontoon.common.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions
import me.mauricee.pontoon.R
import me.mauricee.pontoon.main.MainActivity

class OptionProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        return NotificationOptions.Builder()
                .setActions(listOf(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK, MediaIntentReceiver.ACTION_STOP_CASTING), intArrayOf(0, 1))
                .setTargetActivityClassName(MainActivity.javaClass.name)
                .build()
                .run {
                    CastMediaOptions.Builder()
                            .setNotificationOptions(this)
                            .build()
                }.run {
                    CastOptions.Builder()
                            .setReceiverApplicationId(context.getString(R.string.cast_id))
                            .setStopReceiverApplicationWhenEndingSession(true)
                            .setCastMediaOptions(this)
                            .build()
                }
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider> = mutableListOf()
}