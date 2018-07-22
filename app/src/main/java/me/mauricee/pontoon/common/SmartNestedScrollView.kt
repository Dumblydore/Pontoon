package me.mauricee.pontoon.common

import android.content.Context

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView

open class SmartNestedScrollView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : NestedScrollView(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    override fun measureChildWithMargins(child: View?, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int) {
        if (findNestedRecyclerView(child) != null) {
            val lp = child?.layoutParams as ViewGroup.MarginLayoutParams
            val childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                    lp.topMargin + lp.bottomMargin, View.MeasureSpec.AT_MOST)
            child.measure(parentWidthMeasureSpec, childHeightMeasureSpec)
        } else {
            super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
        }
    }

    private fun findNestedRecyclerView(view: View?): RecyclerView? {
        when (view) {
            is RecyclerView -> return view
            is ViewGroup -> {
                var index = 0
                do {
                    val child = view.getChildAt(index)
                    val recyclerView = findNestedRecyclerView(child)
                    if (recyclerView == null) {
                        index += 1
                    } else {
                        return recyclerView
                    }
                } while (index < view.childCount)
            }
            else -> return null
        }
        return null
    }
}