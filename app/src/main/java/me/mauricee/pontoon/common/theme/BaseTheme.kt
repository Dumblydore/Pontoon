package me.mauricee.pontoon.common.theme

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.DiffUtil
import me.mauricee.pontoon.R

enum class BaseTheme(@StyleRes val style: Int) {
    Light(R.style.AppTheme_Light),
    Dark(R.style.AppTheme_Dark),
    Black(R.style.AppTheme_Black);

    fun theme(context: Context): Resources.Theme = context.resources.newTheme()
            .apply { applyStyle(style, true) }

    companion object {
        val ItemCallback = object : DiffUtil.ItemCallback<BaseTheme>() {
                    override fun areItemsTheSame(oldItem: BaseTheme, newItem: BaseTheme): Boolean = oldItem.ordinal == newItem.ordinal

                    override fun areContentsTheSame(oldItem: BaseTheme, newItem: BaseTheme): Boolean = newItem == oldItem
                }
    }
}