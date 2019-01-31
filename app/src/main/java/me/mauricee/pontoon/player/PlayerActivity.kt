package me.mauricee.pontoon.player

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.fragment_player.*
import me.mauricee.pontoon.BaseActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.main.Player
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.player.player.PlayerContract
import javax.inject.Inject

class PlayerActivity : BaseActivity(), PlayerContract.Controls, GestureEvents {
    @Inject
    lateinit var player: Player
    @Inject
    lateinit var animationTouchListener: VideoTouchHandler
    @Inject
    lateinit var preferences: Preferences

    private var currentScale = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerRoot.setOnTouchListener(animationTouchListener)
        animationTouchListener.pinchToZoomEnabled = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullScreen(true)
        }
    }

    override fun onResume() {
        super.onResume()
        enableFullScreen(true)
    }

    private fun enableFullScreen(isEnabled: Boolean) {
        if (isEnabled) {
            player.viewMode = Player.ViewMode.FullScreen
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun setVideoRatio(ratio: String) {
    }


    override fun toggleFullscreen() {
        onBackPressed()
    }

    override fun setPlayerExpanded(isExpanded: Boolean) {
        onBackPressed()
    }

    override fun onClick(view: View) {
        player.toggleControls()
    }

    override fun onDismiss(view: View) {

    }

    override fun onScale(percentage: Float) {
        player_display.scaleVideo(percentage)
        logd("scale factor: $percentage")

    }

    override fun onSwipe(percentage: Float) {

    }

    override fun onExpand(isExpanded: Boolean) {

    }

    override fun onBackPressed() {
        player.viewMode = Player.ViewMode.Expanded
        super.onBackPressed()
    }
}