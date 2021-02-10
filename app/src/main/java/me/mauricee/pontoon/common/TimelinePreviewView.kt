package me.mauricee.pontoon.common

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import me.mauricee.pontoon.databinding.ViewTimebarBinding
import me.mauricee.pontoon.ext.toDuration
import kotlin.math.max
import kotlin.math.min

class TimelinePreviewView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    private val previewParams: LayoutParams
    private val binding: ViewTimebarBinding
    private var currentSprite: Pair<Int, Bitmap>? = null
    private var currentPreviewAnimation: ViewPropertyAnimator? = null
        set(value) {
            value?.start()
            field = value
        }

    private var maxGuidePercentage: Float = 1f
    private var minGuidePercentage: Float = 0f

    var timelineBitmap: Bitmap? = null
    var frameDuration: Long = 5000L
    var frameWidth: Int = 160
    var duration: Int = 1000
    var progress: Int = 0
        set(value) {
            if (field != value)
                onProgressChanged(value)
            field = value
        }

    init {
        binding = ViewTimebarBinding.inflate(LayoutInflater.from(context), this, true)
        previewParams = binding.previewGuideline.layoutParams as LayoutParams
    }

    fun hide() {
        binding.preview.isGone = true
    }

    private fun onProgressChanged(progress: Int) {
        val progressPercentage = (progress.toFloat() / duration.toFloat())
        binding.preview.isGone = false
        previewParams.guidePercent = min(maxGuidePercentage, max(minGuidePercentage, progressPercentage))

        binding.previewGuideline.layoutParams = previewParams

        val spritePosition = ((duration / frameDuration) * progressPercentage).toInt()
        val sprite = if (currentSprite?.first == spritePosition)
            currentSprite!!.second
        else
            timelineBitmap?.let {
                Bitmap.createBitmap(it, frameWidth * spritePosition, 0, frameWidth, it.height)
            }?.also { currentSprite = Pair(spritePosition, it) }
        binding.previewIcon.setImageBitmap(sprite)
        binding.previewTime.text = (((progress.toFloat() * 1000) / duration.toFloat()) * duration).toLong().toDuration()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            minGuidePercentage = ((binding.preview.measuredWidth / 2) / measuredWidth.toFloat())
            maxGuidePercentage = 1 - minGuidePercentage
        }
    }
}