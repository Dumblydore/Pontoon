package me.mauricee.pontoon.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import me.mauricee.pontoon.common.theme.ThemeManager

class MeTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialTextView(context, attrs, defStyleAttr) {
    private val disposable = ThemeManager.activeTheme.subscribe {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable.dispose()
    }
}