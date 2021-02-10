package me.mauricee.pontoon.ui

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.os.Bundle
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.isupatches.wisefy.WiseFy
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.ActivityMainNewBinding
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.playback.Player
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var wiseFy: WiseFy

    @Inject
    lateinit var prefs: Preferences

    private val binding by viewBinding(ActivityMainNewBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
        themeManager.onCreate(this)
        setContentView(binding.root)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player.isReadyForPiP) goIntoPip()
        else player.pause().blockingAwait()
    }

    private fun goIntoPip() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
            enterPictureInPictureMode(params)
        } else {
            enterPictureInPictureMode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wiseFy.dump()
        player.release()
    }
}

fun Activity.shareVideo(video: Video) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, video.entity.title)
        putExtra(Intent.EXTRA_TEXT, video.toBrowsableUrl())
    }
    startActivity(Intent.createChooser(shareIntent, getString(R.string.player_share)))
}