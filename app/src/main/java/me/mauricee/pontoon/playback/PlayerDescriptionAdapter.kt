package me.mauricee.pontoon.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.media2.common.MediaMetadata

import androidx.media2.session.MediaSession
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import me.mauricee.pontoon.ui.MainActivity
import javax.inject.Inject

class PlayerDescriptionAdapter @Inject constructor(
        private val mediaSession: MediaSession,
        private val context: Context) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.currentMediaItem?.mediaMetadata?.title ?: "Unknown Title"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return mediaSession.player.currentMediaItem?.metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
    }

    override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
        Glide.with(context)
                .asBitmap()
                .load(mediaSession.player.currentMediaItem?.metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART_URI))
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        callback.onBitmap(resource)
                    }
                })
        return null
    }
}