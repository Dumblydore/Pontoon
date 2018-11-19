package me.mauricee.pontoon.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.widget.SeekBarChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarProgressChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStopChangeEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.view_timebar.view.*
import me.mauricee.pontoon.R

class TimeBarView : ConstraintLayout, SeekBar.OnSeekBarChangeListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    private val seekBarEvents: Relay<SeekBarChangeEvent> = PublishRelay.create()
    private val previewParams: ConstraintLayout.LayoutParams

    private var currentSprite: Pair<Int, Bitmap>? = null

    val seekBarChanges: Observable<SeekBarChangeEvent>
        get() = seekBarEvents.hide()

    private var currentAnimation: ViewPropertyAnimator? = null
    private var currentThumbAnimation: ValueAnimator? = null
        set(value) {
            field?.cancel()
            field = value
        }
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
            currentThumbAnimation = animateThumb(if (value) 255 else 0).apply { start() }
        }
    var acceptTapsFromUser: Boolean = true


    init {
        View.inflate(context, R.layout.view_timebar, this)

        previewParams = preview_guideline.layoutParams as ConstraintLayout.LayoutParams
        seekbar.setOnSeekBarChangeListener(this)
        seekbar.setOnTouchListener { view, motionEvent -> !acceptTapsFromUser }
        thumbVisibility = false
    }


    override fun onProgressChanged(seekbar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val progressPercentage = (progress.toFloat() / seekbar.max.toFloat())

            previewParams.guidePercent = progressPercentage
            preview_guideline.layoutParams = previewParams

            val spritePosition = ((duration / frameDuration) * progressPercentage).toInt()
            val sprite = if (currentSprite?.first == spritePosition)
                currentSprite!!.second
            else
                timelineBitmap?.let {
                    Bitmap.createBitmap(it, frameWidth * spritePosition, 0, frameWidth, it.height)
                }?.also { currentSprite = Pair(spritePosition, it) }
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
        preview.isVisible = true
        thumbVisibility = false
        currentAnimation = preview.animate().alpha(0f)
                .setDuration(250)
                .apply { start() }
        seekBarEvents.accept(SeekBarStopChangeEvent.create(seekbar))
    }

    private fun animateThumb(to: Int): ValueAnimator {
        return ValueAnimator.ofInt(seekbar.thumb.alpha, to).apply {
            addUpdateListener {
                val thumb = seekbar.thumb.mutate()
                val value = it.animatedValue as Int
                thumb.alpha = value
            }
            startDelay = 500
        }
    }
}