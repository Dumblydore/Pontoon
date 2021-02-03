package me.mauricee.pontoon.common

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.scopes.ActivityScoped
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ext.just
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class ShareManager @Inject constructor(private val context: AppCompatActivity) {
    fun shareVideo(video: Video) = context.just {
        startActivity(Intent.createChooser(createShareIntent(video), getString(R.string.player_share)))
    }

    private fun createShareIntent(video: Video) = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, video.entity.title)
        putExtra(Intent.EXTRA_TEXT, video.toBrowsableUrl())
    }
}