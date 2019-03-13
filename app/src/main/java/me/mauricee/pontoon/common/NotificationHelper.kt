package me.mauricee.pontoon.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import me.mauricee.pontoon.R
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.ext.logd
import javax.inject.Inject


@AppScope
class NotificationHelper @Inject constructor(private val notificationManager: NotificationManager,
                                             private val context: Context) {

    private var notificationId: Int = 0

    fun importantNotification(intent: Intent, channel: String, builder: (builder: NotificationCompat.Builder) -> Unit) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        NotificationCompat.Builder(context, ChannelId).apply {
            setSmallIcon(R.drawable.ic_notification)
            setAutoCancel(false)
            setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            setChannelId(ChannelId)
            builder(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannel(channel)
            }
        }.build().let {
            logd("Notifying!")
            notificationManager.notify(notificationId++, it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setChannel(channel: String, importance: Int = NotificationManager.IMPORTANCE_DEFAULT) {
        val notificationChannel = NotificationChannel(ChannelId, channel, importance)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private const val ChannelId = "10001"
        const val LiveStreamNotificationChannel = "LiveStreams"
    }

}