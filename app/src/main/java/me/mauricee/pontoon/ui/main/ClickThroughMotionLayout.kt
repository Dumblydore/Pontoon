package me.mauricee.pontoon.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.constraintlayout.motion.widget.MotionLayout
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import me.mauricee.pontoon.R
import kotlin.math.roundToInt

class ClickThroughMotionLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {
    //Hardcoded
    private val whitelistedViews = listOf(R.id.playerControlPlayPause,
            R.id.playerControlsFullscreen,
            R.id.playerControlQuality,
            R.id.playerControlsExpand,
            R.id.playerControlMenu,
            R.id.playerControlCast)
    private var startX: Int = 0
    private var startY: Int = 0
    private var startTime: Long = 0L
    private val _playerClicks: Relay<Unit> = PublishRelay.create()
    val playerClicks: Observable<Unit>
        get() = _playerClicks.hide()
    var allowPlayerClick: Boolean = true

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        super.onInterceptTouchEvent(event)
        return false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = findViewById<View>(R.id.playerTouchBox)

        if (isInBounds(view, ev) && !whitelistedViews
                .mapNotNull(this::findViewById)
                .any { isInBounds(it, ev) }) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.x.roundToInt()
                    startY = ev.y.roundToInt()
                    startTime = ev.eventTime
                }
                MotionEvent.ACTION_UP -> {
                    val endX = ev.x.roundToInt()
                    val endY = ev.y.roundToInt()
                    if (isAClick(startX, endX, startY, endY) && ev.eventTime - startTime <= ViewConfiguration.getTapTimeout()) {
                        _playerClicks.accept(Unit)
                        return true
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isInBounds(view: View, ev: MotionEvent): Boolean {
        val (x, y) = ev.x.roundToInt() to ev.y.roundToInt()
        return x > view.left && x < view.right && y > view.top && y < view.bottom
    }

    private fun isAClick(startX: Int, endX: Int, startY: Int, endY: Int): Boolean {
        val differenceX = Math.abs(startX - endX)
        val differenceY = Math.abs(startY - endY)
        return !(differenceX > 200 || differenceY > 200)
    }
}