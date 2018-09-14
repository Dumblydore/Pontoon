package me.mauricee.pontoon.common


import android.content.Context
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatImageView

class TileImageView : AppCompatImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //   let the default measuring occur, then force the desired aspect ratio
        //   on the view (not the drawable).
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val newMeasurement = measuredWidth
        //force a 3:2 aspect ratio
        setMeasuredDimension(newMeasurement, newMeasurement)
    }
}
