package me.mauricee.pontoon.common

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding2.widget.SeekBarChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarProgressChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStopChangeEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.view_timebar.view.*
import me.mauricee.pontoon.R

class TimeBarView(context: Context?) : ConstraintLayout(context), SeekBar.OnSeekBarChangeListener {

    private val seekBarEvents: Relay<SeekBarChangeEvent> = PublishRelay.create()
    private val paramsGlMarginStart: ConstraintLayout.LayoutParams
    private val paramsGlMarginEnd: ConstraintLayout.LayoutParams

    private var currentSprite: Pair<Int, Bitmap>? = null

    val seekBarChanges: Observable<SeekBarChangeEvent>
        get() = seekBarEvents.hide()

    var currentAnimation: ViewPropertyAnimator? = null
    var timelineBitmap: Bitmap? = null
    var frameDuration: Long = 5000L
    var frameWidth: Int = 160
    var duration: Long
        get() = (seekbar.max * 1000).toLong()
        set(value) {
            seekbar.max = (value / 1000).toInt()
        }
    var progress: Long
        set(value) {
            seekbar.progress = (value / 1000).toInt()
        }
        get() = seekbar.progress * 1000L
    var bufferedProgress: Long
        set(value) {
            seekbar.secondaryProgress = (value / 1000).toInt()
        }
        get() = seekbar.secondaryProgress * 1000L

    var thumbVisibility: Boolean
        set(value) {
            seekbar.thumb.mutate().setVisible(value, true)
        }
        get() = seekbar.thumb.isVisible


    init {
        View.inflate(context, R.layout.view_timebar, this)

        paramsGlMarginStart = guidelineMarginStart.layoutParams as ConstraintLayout.LayoutParams
        paramsGlMarginEnd = guidelineMarginEnd.layoutParams as ConstraintLayout.LayoutParams
        seekbar.setOnSeekBarChangeListener(this)
    }


    override fun onProgressChanged(seekbar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val startPercentage = Math.min(MAX_MARGIN_START_LIMIT, Math.max((duration / progress).toFloat(), MIN_MARGIN_START_LIMIT))
            val endPercentage = Math.min(startPercentage + MARGIN_DIFF, MIN_MARGIN_END_LIMIT)

            paramsGlMarginStart.guidePercent = startPercentage
            paramsGlMarginEnd.guidePercent = endPercentage

            guidelineMarginStart.layoutParams = paramsGlMarginStart
            guidelineMarginEnd.layoutParams = paramsGlMarginEnd

            val spritePosition = (duration / frameDuration).toInt()
            val sprite = if (currentSprite?.first == spritePosition)
                currentSprite!!.second
            else
                timelineBitmap?.let {
                    Bitmap.createBitmap(it, frameWidth * spritePosition, 0, frameWidth, it.height)
                }.also { currentSprite = Pair(spritePosition, it!!) }
            preview.setImageBitmap(sprite)
        }
        seekBarEvents.accept(SeekBarProgressChangeEvent.create(seekbar, progress, fromUser))
    }

    override fun onStartTrackingTouch(seekbar: SeekBar) {
        currentAnimation?.cancel()
        currentAnimation = preview.animate().alpha(1f)
                .setDuration(250)
                .apply { start() }
        seekBarEvents.accept(SeekBarStartChangeEvent.create(seekbar))
    }

    override fun onStopTrackingTouch(seekbar: SeekBar) {
        currentAnimation = preview.animate().alpha(0f)
                .setDuration(250)
                .apply { start() }
        seekBarEvents.accept(SeekBarStopChangeEvent.create(seekbar))
    }

    companion object {
        const val MIN_MARGIN_START_LIMIT = 0.025F
        const val MIN_MARGIN_END_LIMIT = 0.975F
        const val MARGIN_DIFF = 0.225F
        const val MAX_MARGIN_START_LIMIT = MIN_MARGIN_END_LIMIT - MARGIN_DIFF
    }

}